package com.app.yeoback_backend.mybatis.handler;

import com.app.yeoback_backend.common.enumeration.OAuthProvider;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * OAuth 공급자(oauth_provider) ↔ PostgreSQL enum 변환.
 * DB에는 소문자 값(OAuthProvider.getValue())으로 저장한다.
 * BaseTypeHandler 를 상속해서 null 값은 MyBatis 가 알아서 처리한다.
 */
@MappedTypes(OAuthProvider.class)
public class OAuthProviderHandler extends BaseTypeHandler<OAuthProvider> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OAuthProvider parameter, JdbcType jdbcType) throws SQLException {
        // Types.OTHER: PostgreSQL enum 컬럼에 문자열을 넣을 때 필요
        ps.setObject(i, parameter.getValue(), Types.OTHER);
    }

    @Override
    public OAuthProvider getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toEnum(rs.getString(columnName));
    }

    @Override
    public OAuthProvider getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toEnum(rs.getString(columnIndex));
    }

    @Override
    public OAuthProvider getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toEnum(cs.getString(columnIndex));
    }

    private OAuthProvider toEnum(String value) {
        return value == null ? null : OAuthProvider.getOAuthProvider(value);
    }
}
