package com.monitor.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.dao.PerformanceSummaryMapper;
import com.monitor.dao.IssueMapper;
import com.monitor.dao.AlertInstanceMapper;
import com.monitor.dto.ChatRequestDTO;
import com.monitor.dto.ChatResponseVO;
import com.monitor.dto.DiagnoseRequestDTO;
import com.monitor.dto.DiagnoseResponseVO;
import com.monitor.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * DeepSeek AI 服务实现类
 * 调用 DeepSeek OpenAI 兼容 API
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PerformanceSummaryMapper performanceSummaryMapper;

    @Autowired
    private IssueMapper issueMapper;

    @Autowired
    private AlertInstanceMapper alertInstanceMapper;

    /** 系统提示词：让 AI 了解 APM 监控平台上下文 */
    private static final String SYSTEM_PROMPT = """
        你是一个专业的移动应用性能监控(APM)平台的智能助手。你具备以下能力：
        
        1. 性能数据分析：帮助用户分析崩溃率、FPS、启动时长、内存占用、CPU使用率等指标
        2. 告警解读：解释告警原因并提供优化建议
        3. 问题诊断：帮助定位性能瓶颈和应用异常问题
        4. 技术建议：针对移动应用(Android/iOS)提供性能优化方案
        
        请用简洁专业的中文回答。如果用户的问题与移动应用性能监控无关，请礼貌地引导回正题。
        回答时尽量结构化，适当使用列表和加粗。
        """;

    @Override
    public ChatResponseVO chat(ChatRequestDTO request) {
        // 未配置 API Key 时给出明确提示，而不是抛异常被转成 400 通用错误
        if (!isApiKeyConfigured()) {
            return buildUnavailableChatResponse();
        }
        try {
            Map<String, Object> requestBody = buildRequestBody(request, SYSTEM_PROMPT);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            log.info("调用 DeepSeek API...");
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            return parseResponse(response.getBody());
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试: " + e.getMessage());
        }
    }

    @Override
    public DiagnoseResponseVO diagnose(DiagnoseRequestDTO request) {
        // 未配置 API Key 时给出明确提示
        if (!isApiKeyConfigured()) {
            return buildUnavailableDiagnoseResponse(request.getAppKey());
        }
        try {
            // 1. 拉取应用的性能数据
            String appKey = request.getAppKey();
            Map<String, Object> metricsData = fetchMetricsData(appKey, request.getStartTime(), request.getEndTime());

            // 2. 构建诊断 Prompt
            String diagnosisPrompt = buildDiagnosisPrompt(appKey, metricsData, request.getDimension());

            // 3. 调用 DeepSeek API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 2048);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个专业的移动应用性能诊断专家。请基于提供的性能指标数据，生成结构化的诊断报告。要求：1) 分析各项指标的健康状况 2) 指出潜在风险 3) 给出具体的优化建议 4) 输出风险等级。用Markdown格式回复，包含表格和列表。");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", diagnosisPrompt);
            messages.add(userMsg);

            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用 DeepSeek 智能诊断 API...");
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            ChatResponseVO chatResponse = parseResponse(response.getBody());

            // 4. 构建诊断响应
            DiagnoseResponseVO.KeyMetrics keyMetrics = buildKeyMetrics(metricsData);
            String riskLevel = evaluateRiskLevel(keyMetrics);

            // 解析AI返回的建议
            List<DiagnoseResponseVO.Suggestion> suggestions = extractSuggestions(chatResponse.getContent(), keyMetrics);

            return DiagnoseResponseVO.builder()
                    .report(chatResponse.getContent())
                    .keyMetrics(keyMetrics)
                    .riskLevel(riskLevel)
                    .suggestions(suggestions)
                    .model(chatResponse.getModel())
                    .build();

        } catch (Exception e) {
            log.error("AI 智能诊断失败: {}", e.getMessage(), e);
            // 返回兜底诊断结果
            return DiagnoseResponseVO.builder()
                    .report("**诊断失败**：AI 服务暂时不可用。\n\n错误信息：" + e.getMessage())
                    .keyMetrics(DiagnoseResponseVO.KeyMetrics.builder()
                            .crashRate(0.0).avgStartupTime(0.0).lagRate(0.0)
                            .networkSuccessRate(100.0).activeUsers(0).alertCount(0).build())
                    .riskLevel("UNKNOWN")
                    .suggestions(Collections.emptyList())
                    .model("N/A")
                    .build();
        }
    }

    /**
     * 从数据库拉取应用的性能指标数据
     */
    private Map<String, Object> fetchMetricsData(String appKey, String startTime, String endTime) {
        Map<String, Object> data = new HashMap<>();
        try {
            // 时间参数容错：字符串为空或格式非法时回退到最近 7 天
            LocalDateTime start = parseDateTime(startTime, LocalDateTime.now().minusDays(7));
            LocalDateTime end = parseDateTime(endTime, LocalDateTime.now());

            // 崩溃率 / 崩溃总数（来自汇总表聚合，避免读取表中不存在的列）
            Map<String, Object> crashStats = performanceSummaryMapper.selectCrashStats(appKey, start, end, "day");
            data.put("crashRate", toDouble(crashStats == null ? null : crashStats.get("crashRate")));
            data.put("totalCrash", (long) toDouble(crashStats == null ? null : crashStats.get("totalCrashes")));

            // 网络成功率（%）
            Map<String, Object> netStats = performanceSummaryMapper.selectNetworkStats(appKey, start, end, "day");
            double successRate = toDouble(netStats == null ? null : netStats.get("successRate"));
            data.put("networkSuccessRate", netStats == null ? 100.0 : successRate);

            // 平均启动时间 + 活跃用户总数（按天趋势聚合）
            List<Map<String, Object>> trend = performanceSummaryMapper.selectTrendData(appKey, start, end);
            if (trend != null && !trend.isEmpty()) {
                double avgStartupTime = trend.stream()
                        .mapToDouble(r -> toDouble(r.get("avgStartupTime")))
                        .average().orElse(0.0);
                long totalUsers = trend.stream()
                        .mapToLong(r -> (long) toDouble(r.get("activeUsers")))
                        .sum();
                data.put("avgStartupTime", avgStartupTime);
                data.put("activeUsers", totalUsers);   // 活跃用户总数，不再除以行数
                data.put("dataPoints", trend.size());
            } else {
                data.put("avgStartupTime", 0.0);
                data.put("activeUsers", 0L);
                data.put("dataPoints", 0);
            }

            // ANR 总数（省份维度求和）
            List<Map<String, Object>> regions = performanceSummaryMapper.selectRegionDistribution(appKey, start, end, null);
            long totalAnr = regions == null ? 0L
                    : regions.stream().mapToLong(r -> (long) toDouble(r.get("anrCount"))).sum();
            data.put("totalAnr", totalAnr);

            // 告警数量
            var alerts = alertInstanceMapper.selectListByQuery(
                    new com.mybatisflex.core.query.QueryWrapper()
                            .where("app_key = ?", appKey)
            );
            data.put("alertCount", alerts != null ? alerts.size() : 0);
            data.put("firingAlerts", alerts != null ?
                    alerts.stream().filter(a -> "FIRING".equals(a.getStatus() != null ? a.getStatus().name() : "")).count() : 0);

            // 问题数量
            var issues = issueMapper.selectListByQuery(
                    new com.mybatisflex.core.query.QueryWrapper()
                            .where("app_key = ?", appKey)
            );
            data.put("issueCount", issues != null ? issues.size() : 0);

        } catch (Exception e) {
            log.warn("拉取指标数据时出错: {}", e.getMessage());
            data.put("crashRate", 0.0);
            data.put("avgStartupTime", 0.0);
            data.put("networkSuccessRate", 100.0);
            data.put("activeUsers", 0);
            data.put("alertCount", 0);
        }
        return data;
    }

    /**
     * 判断 DeepSeek API Key 是否已配置
     */
    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 未配置 API Key 时的对话降级响应
     */
    private ChatResponseVO buildUnavailableChatResponse() {
        ChatResponseVO vo = new ChatResponseVO();
        vo.setContent("**AI 助手未启用**\n\n未检测到 DeepSeek API Key。\n\n"
                + "请在项目根目录的 `.env` 中配置 `DEEPSEEK_API_KEY=你的密钥`，然后重启后端即可使用。"
                + "（其余功能不受影响）");
        vo.setModel("N/A");
        return vo;
    }

    /**
     * 未配置 API Key 时的诊断降级响应
     */
    private DiagnoseResponseVO buildUnavailableDiagnoseResponse(String appKey) {
        return DiagnoseResponseVO.builder()
                .report("**AI 诊断未启用**\n\n未检测到 DeepSeek API Key，无法生成智能诊断报告。\n\n"
                        + "请在项目根目录的 `.env` 中配置 `DEEPSEEK_API_KEY=你的密钥`，然后重启后端。")
                .keyMetrics(DiagnoseResponseVO.KeyMetrics.builder()
                        .crashRate(0.0).avgStartupTime(0.0).lagRate(0.0)
                        .networkSuccessRate(100.0).activeUsers(0).alertCount(0).build())
                .riskLevel("UNKNOWN")
                .suggestions(Collections.emptyList())
                .model("N/A")
                .build();
    }

    /**
     * 解析时间字符串，失败时返回默认值
     */
    private LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            log.warn("时间参数格式非法，使用默认值: {}", value);
            return defaultValue;
        }
    }

    /**
     * 安全转换为 double，null 或非数字时返回 0
     */
    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 构建 AI 诊断 Prompt
     */
    private String buildDiagnosisPrompt(String appKey, Map<String, Object> data, String dimension) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下移动应用性能数据进行全面诊断分析：\n\n");
        sb.append("**应用标识**：").append(appKey).append("\n\n");
        sb.append("**性能指标数据**：\n");
        sb.append("| 指标 | 数值 |\n");
        sb.append("|------|------|\n");
        sb.append("| 崩溃率 | ").append(String.format("%.4f%%", getDouble(data, "crashRate"))).append(" |\n");
        sb.append("| 平均启动时间 | ").append(String.format("%.0f ms", getDouble(data, "avgStartupTime"))).append(" |\n");
        sb.append("| 崩溃总次数 | ").append(data.getOrDefault("totalCrash", 0)).append(" |\n");
        sb.append("| ANR总次数 | ").append(data.getOrDefault("totalAnr", 0)).append(" |\n");
        sb.append("| 网络成功率 | ").append(String.format("%.2f%%", getDouble(data, "networkSuccessRate"))).append(" |\n");
        sb.append("| 活跃用户数 | ").append(data.getOrDefault("activeUsers", 0)).append(" |\n");
        sb.append("| 告警总数 | ").append(data.getOrDefault("alertCount", 0)).append(" |\n");
        sb.append("| 活跃告警数 | ").append(data.getOrDefault("firingAlerts", 0)).append(" |\n");
        sb.append("| 问题总数 | ").append(data.getOrDefault("issueCount", 0)).append(" |\n");
        sb.append("| 数据样本数 | ").append(data.getOrDefault("dataPoints", 0)).append(" |\n");
        sb.append("\n");

        if (!"all".equals(dimension)) {
            sb.append("**重点诊断维度**：").append(dimension).append("\n\n");
        }

        sb.append("请输出结构化的诊断报告，包含：\n");
        sb.append("1. **总体健康评分**（0-100分）\n");
        sb.append("2. **各项指标分析**（正常/警告/危险）\n");
        sb.append("3. **潜在风险点**\n");
        sb.append("4. **具体优化建议**（按优先级排序）\n");
        sb.append("5. **综合风险等级**（低/中/高/严重）\n");

        return sb.toString();
    }

    private DiagnoseResponseVO.KeyMetrics buildKeyMetrics(Map<String, Object> data) {
        return DiagnoseResponseVO.KeyMetrics.builder()
                .crashRate(getDouble(data, "crashRate"))
                .avgStartupTime(getDouble(data, "avgStartupTime"))
                .lagRate(0.0)
                .networkSuccessRate(getDouble(data, "networkSuccessRate"))
                .activeUsers(((Number) data.getOrDefault("activeUsers", 0)).intValue())
                .alertCount(((Number) data.getOrDefault("alertCount", 0)).intValue())
                .build();
    }

    private String evaluateRiskLevel(DiagnoseResponseVO.KeyMetrics km) {
        double crashRate = km.getCrashRate() != null ? km.getCrashRate() * 100 : 0;
        if (crashRate > 5.0) return "CRITICAL";
        if (crashRate > 2.0) return "HIGH";
        if (crashRate > 1.0) return "MEDIUM";
        return "LOW";
    }

    private List<DiagnoseResponseVO.Suggestion> extractSuggestions(String content, DiagnoseResponseVO.KeyMetrics km) {
        // 基于指标自动生成基础建议
        List<DiagnoseResponseVO.Suggestion> suggestions = new ArrayList<>();
        double crashRate = km.getCrashRate() != null ? km.getCrashRate() * 100 : 0;
        if (crashRate > 1.0) {
            suggestions.add(DiagnoseResponseVO.Suggestion.builder()
                    .category("稳定性").title("降低崩溃率").description("当前崩溃率为 " + String.format("%.2f%%", crashRate) + "，建议排查高频崩溃堆栈，优先修复 Top 5 崩溃问题。").priority("HIGH").build());
        }
        double startupTime = km.getAvgStartupTime() != null ? km.getAvgStartupTime() : 0;
        if (startupTime > 2000) {
            suggestions.add(DiagnoseResponseVO.Suggestion.builder()
                    .category("启动优化").title("优化启动时长").description("平均启动时间 " + String.format("%.0f", startupTime) + "ms，建议使用启动任务懒加载、延迟初始化等策略优化。").priority("MEDIUM").build());
        }
        if (km.getAlertCount() > 5) {
            suggestions.add(DiagnoseResponseVO.Suggestion.builder()
                    .category("监控").title("关注活跃告警").description("当前存在 " + km.getAlertCount() + " 条告警，及时处理避免问题积累。").priority("MEDIUM").build());
        }
        return suggestions;
    }

    private double getDouble(Map<String, Object> data, String key) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    /**
     * 构建 DeepSeek API 请求体
     */
    private Map<String, Object> buildRequestBody(ChatRequestDTO request, String systemPrompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        if (request.getMessages() != null) {
            for (ChatRequestDTO.Message msg : request.getMessages()) {
                Map<String, String> historyMsg = new HashMap<>();
                historyMsg.put("role", msg.getRole());
                historyMsg.put("content", msg.getContent());
                messages.add(historyMsg);
            }
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getContent());
        messages.add(userMsg);

        body.put("messages", messages);
        return body;
    }

    /**
     * 解析 DeepSeek API 响应
     */
    private ChatResponseVO parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (choices.isEmpty()) {
            throw new RuntimeException("AI 返回了空响应");
        }
        String content = choices.get(0).path("message").path("content").asText("");
        String returnedModel = root.path("model").asText(model);
        int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
        int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
        return new ChatResponseVO(content, returnedModel, promptTokens, completionTokens);
    }
}
