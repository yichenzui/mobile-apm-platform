package com.monitor.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JSON类型处理器，用于处理JsonNode类型的数据库字段
 */
@MappedTypes(JsonNode.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, parameter.toString());
        } catch (Exception e) {
            throw new SQLException("Error converting JsonNode to String", e);
        }
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private JsonNode parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON: " + json, e);
        }
    }
}
