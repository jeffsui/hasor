/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.jdbc.core;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import net.hasor.core.Hasor;
import net.hasor.jdbc.BatchPreparedStatementSetter;
import net.hasor.jdbc.CallableStatementCallback;
import net.hasor.jdbc.CallableStatementCreator;
import net.hasor.jdbc.ConnectionCallback;
import net.hasor.jdbc.JdbcOperations;
import net.hasor.jdbc.PreparedStatementCallback;
import net.hasor.jdbc.PreparedStatementCreator;
import net.hasor.jdbc.PreparedStatementSetter;
import net.hasor.jdbc.ResultSetExtractor;
import net.hasor.jdbc.RowCallbackHandler;
import net.hasor.jdbc.RowMapper;
import net.hasor.jdbc.SqlParameterSource;
import net.hasor.jdbc.SqlRowSet;
import net.hasor.jdbc.StatementCallback;
import net.hasor.jdbc.core.mapper.BeanPropertyRowMapper;
import net.hasor.jdbc.core.mapper.ColumnMapRowMapper;
import net.hasor.jdbc.core.mapper.SingleColumnRowMapper;
import net.hasor.jdbc.core.source.MapSqlParameterSource;
import net.hasor.jdbc.core.util.JdbcUtils;
import net.hasor.jdbc.core.util.NamedBatchUpdateUtils;
import net.hasor.jdbc.core.util.NamedParameterUtils;
import net.hasor.jdbc.core.util.ParsedSql;
import net.hasor.jdbc.core.util.PreparedStatementCreatorFactory;
import net.hasor.jdbc.datasource.ConnectionProxy;
import net.hasor.jdbc.datasource.DataSourceUtils;
import net.hasor.jdbc.exceptions.DataAccessException;
import net.hasor.jdbc.exceptions.InvalidDataAccessException;
import net.hasor.jdbc.exceptions.SQLWarningException;
import org.more.util.ArrayUtils;
import org.more.util.IOUtils;
import org.more.util.ResourcesUtils;
/**
 * 数据库操作模板方法。
 * @version : 2013-10-12
 * @author 赵永春 (zyc@byshell.org)
 */
public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {
    /** 默认模板最大缓存 SQL语句条数 : 256 */
    public static final int        DEFAULT_CACHE_LIMIT    = 256;
    /*是否忽略出现的 SQL 警告*/
    private boolean                ignoreWarnings         = true;
    /*JDBC查询和从结果集里面每次取设置行数，循环去取，直到取完。合理设置该参数可以避免内存异常。
     * 如果这个变量被设置为非零值,它将被用于设置 statements 的 fetchSize 属性。*/
    private int                    fetchSize              = 0;
    /*从 JDBC 中可以查询的最大行数。
     * 如果这个变量被设置为非零值,它将被用于设置 statements 的 maxRows 属性。*/
    private int                    maxRows                = 0;
    /*从 JDBC 中可以查询的最大行数。
     * 如果这个变量被设置为非零值,它将被用于设置 statements 的 queryTimeout 属性。*/
    private int                    queryTimeout           = 0;
    /*当JDBC 结果集中如出现相同的列名仅仅大小写不同时。是否保留大小写列名敏感。
     * 如果为 true 表示敏感，并且结果集Map中保留两个记录。如果为 false 则表示不敏感，如出现冲突列名后者将会覆盖前者。*/
    private boolean                resultsCaseInsensitive = false;
    /*ParsedSql SQL 缓存大小*/
    private volatile int           cacheLimit             = DEFAULT_CACHE_LIMIT;
    /*缓存因 ParsedSql 产生的SQL语句 */
    private Map<String, ParsedSql> parsedSqlCache;
    //
    //
    //
    /**
     * Construct a new JdbcTemplate for bean usage.
     * <p>Note: The DataSource has to be set before using the instance.
     * @see #setDataSource
     */
    public JdbcTemplate() {
        this.parsedSqlCache = new LinkedHashMap<String, ParsedSql>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
            private static final long serialVersionUID = -4651854332831321954L;
            protected boolean removeEldestEntry(Map.Entry<String, ParsedSql> eldest) {
                return size() > getCacheLimit();
            }
        };
    }
    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public JdbcTemplate(DataSource dataSource) {
        this();
        setDataSource(dataSource);
    }
    //
    //
    //
    public boolean isIgnoreWarnings() {
        return ignoreWarnings;
    }
    public void setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }
    public int getFetchSize() {
        return fetchSize;
    }
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
    public int getMaxRows() {
        return maxRows;
    }
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }
    public int getQueryTimeout() {
        return queryTimeout;
    }
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
    public boolean isResultsCaseInsensitive() {
        return resultsCaseInsensitive;
    }
    public void setResultsCaseInsensitive(boolean resultsCaseInsensitive) {
        this.resultsCaseInsensitive = resultsCaseInsensitive;
    }
    /**Specify the maximum number of entries for this template's SQL cache. Default is 256.*/
    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }
    /**Return the maximum number of entries for this template's SQL cache.*/
    public int getCacheLimit() {
        return this.cacheLimit;
    }
    //
    //
    public void loadSQL(String sqlResource) throws IOException {
        InputStream inStream = ResourcesUtils.getResourceAsStream(sqlResource);
        if (inStream == null)
            throw new IOException("can't find :" + sqlResource);
        StringWriter outWriter = new StringWriter();
        IOUtils.copy(inStream, outWriter);
        this.execute(outWriter.toString());
    }
    public void loadSQL(Reader sqlReader) throws IOException {
        StringWriter outWriter = new StringWriter();
        IOUtils.copy(sqlReader, outWriter);
        this.execute(outWriter.toString());
    }
    //
    //
    public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
        Hasor.assertIsNotNull(action, "Callback object must not be null");
        Connection con = DataSourceUtils.getConnection(this.getDataSource());
        con = this.newProxyConnection(con);//申请本地连接（和当前线程绑定的连接）
        try {
            return action.doInConnection(con);
        } catch (SQLException ex) {
            throw new DataAccessException("ConnectionCallback SQL :" + getSql(action), ex);
        } finally {
            DataSourceUtils.releaseConnection(con, this.getDataSource());//关闭或释放连接
        }
    }
    public <T> T execute(StatementCallback<T> action) throws DataAccessException {
        Hasor.assertIsNotNull(action, "Callback object must not be null");
        Connection con = DataSourceUtils.getConnection(this.getDataSource());
        con = this.newProxyConnection(con);//申请本地连接（和当前线程绑定的连接）
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            applyStatementSettings(stmt);
            T result = action.doInStatement(stmt);
            handleWarnings(stmt);
            return result;
        } catch (SQLException ex) {
            throw new DataAccessException("StatementCallback SQL :" + getSql(action), ex);
        } finally {
            JdbcUtils.closeStatement(stmt);
            DataSourceUtils.releaseConnection(con, this.getDataSource());//关闭或释放连接
            stmt = null;
            con = null;
        }
    }
    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
        Hasor.assertIsNotNull(psc, "PreparedStatementCreator must not be null");
        Hasor.assertIsNotNull(action, "Callback object must not be null");
        if (Hasor.isDebugLogger()) {
            String sql = getSql(psc);
            Hasor.logDebug("Executing prepared SQL statement " + (sql != null ? " [" + sql + "]" : ""));
        }
        Connection con = DataSourceUtils.getConnection(this.getDataSource());
        con = this.newProxyConnection(con);//申请本地连接（和当前线程绑定的连接）
        PreparedStatement ps = null;
        try {
            ps = psc.createPreparedStatement(con);
            applyStatementSettings(ps);
            T result = action.doInPreparedStatement(ps);
            handleWarnings(ps);
            return result;
        } catch (SQLException ex) {
            throw new DataAccessException("PreparedStatementCallback SQL :" + getSql(psc), ex);
        } finally {
            if (psc instanceof ParameterDisposer)
                ((ParameterDisposer) psc).cleanupParameters();
            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(con, this.getDataSource());//关闭或释放连接
            ps = null;
            con = null;
        }
    }
    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
        Hasor.assertIsNotNull(csc, "CallableStatementCreator must not be null");
        Hasor.assertIsNotNull(action, "Callback object must not be null");
        if (Hasor.isDebugLogger()) {
            String sql = getSql(csc);
            Hasor.logDebug("Calling stored procedure" + (sql != null ? " [" + sql + "]" : ""));
        }
        Connection con = DataSourceUtils.getConnection(this.getDataSource());
        con = this.newProxyConnection(con);//申请本地连接（和当前线程绑定的连接）
        CallableStatement cs = null;
        try {
            cs = csc.createCallableStatement(con);
            applyStatementSettings(cs);
            T result = action.doInCallableStatement(cs);
            handleWarnings(cs);
            return result;
        } catch (SQLException ex) {
            throw new DataAccessException("CallableStatementCallback SQL :" + getSql(action), ex);
        } finally {
            if (csc instanceof ParameterDisposer)
                ((ParameterDisposer) csc).cleanupParameters();
            JdbcUtils.closeStatement(cs);
            DataSourceUtils.releaseConnection(con, this.getDataSource());//关闭或释放连接
        }
    }
    public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
        return execute(new SimplePreparedStatementCreator(sql), action);
    }
    public <T> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException {
        return execute(new SimpleCallableStatementCreator(callString), action);
    }
    public <T> T execute(String sql, SqlParameterSource paramSource, PreparedStatementCallback<T> action) throws DataAccessException {
        return execute(getPreparedStatementCreator(sql, paramSource), action);
    }
    public <T> T execute(String sql, Map<String, ?> paramMap, PreparedStatementCallback<T> action) throws DataAccessException {
        return execute(sql, new MapSqlParameterSource(paramMap), action);
    }
    //
    //
    //
    public void execute(final String sql) throws DataAccessException {
        Hasor.logDebug("Executing SQL statement [%s].", sql);
        class ExecuteStatementCallback implements StatementCallback<Object>, SqlProvider {
            public Object doInStatement(Statement stmt) throws SQLException {
                stmt.execute(sql);
                return null;
            }
            public String getSql() {
                return sql;
            }
        }
        this.execute(new ExecuteStatementCallback());
    }
    //
    //
    //
    /***/
    public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws DataAccessException {
        Hasor.assertIsNotNull(rse, "ResultSetExtractor must not be null");
        Hasor.logDebug("Executing prepared SQL query");
        return execute(psc, new PreparedStatementCallback<T>() {
            public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
                ResultSet rs = null;
                try {
                    if (pss != null)
                        pss.setValues(ps);
                    rs = ps.executeQuery();
                    return rse.extractData(rs);
                } finally {
                    rs.close();
                    if (pss instanceof ParameterDisposer)
                        ((ParameterDisposer) pss).cleanupParameters();
                }
            }
        });
    }
    public <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(psc, null, rse);
    }
    public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws DataAccessException {
        Hasor.assertIsNotNull(sql, "SQL must not be null");
        Hasor.assertIsNotNull(rse, "ResultSetExtractor must not be null");
        Hasor.logDebug("Executing SQL query [%s].", sql);
        class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
            public T doInStatement(Statement stmt) throws SQLException {
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery(sql);
                    return rse.extractData(rs);
                } finally {
                    JdbcUtils.closeResultSet(rs);
                    rs = null;
                }
            }
            public String getSql() {
                return sql;
            }
        }
        return execute(new QueryStatementCallback());
    }
    public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(new SimplePreparedStatementCreator(sql), pss, rse);
    }
    public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException {
        return query(sql, newArgPreparedStatementSetter(args), rse);
    }
    public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(sql, newArgPreparedStatementSetter(args), rse);
    }
    public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(sql, newArgTypePreparedStatementSetter(args, argTypes), rse);
    }
    public <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(getPreparedStatementCreator(sql, paramSource), rse);
    }
    public <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(sql, new MapSqlParameterSource(paramMap), rse);
    }
    //
    //
    //
    public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException {
        query(psc, new RowCallbackHandlerResultSetExtractor(rch));
    }
    public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
        query(sql, new RowCallbackHandlerResultSetExtractor(rch));
    }
    public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException {
        query(sql, pss, new RowCallbackHandlerResultSetExtractor(rch));
    }
    public void query(String sql, RowCallbackHandler rch, Object... args) throws DataAccessException {
        query(sql, newArgPreparedStatementSetter(args), rch);
    }
    public void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException {
        query(sql, newArgPreparedStatementSetter(args), rch);
    }
    public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException {
        query(sql, newArgTypePreparedStatementSetter(args, argTypes), rch);
    }
    public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch) throws DataAccessException {
        query(getPreparedStatementCreator(sql, paramSource), rch);
    }
    public void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch) throws DataAccessException {
        query(sql, new MapSqlParameterSource(paramMap), rch);
    }
    //
    //
    //
    public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
        return query(psc, new RowMapperResultSetExtractor<T>(rowMapper));
    }
    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, pss, new RowMapperResultSetExtractor<T>(rowMapper));
    }
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper));
    }
    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper));
    }
    public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, args, argTypes, new RowMapperResultSetExtractor<T>(rowMapper));
    }
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new RowMapperResultSetExtractor<T>(rowMapper));
    }
    public <T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws DataAccessException {
        return query(getPreparedStatementCreator(sql, paramSource), rowMapper);
    }
    public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, new MapSqlParameterSource(paramMap), rowMapper);
    }
    //
    //
    //
    public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException {
        return query(sql, getBeanPropertyRowMapper(elementType));
    }
    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws DataAccessException {
        return query(sql, args, getBeanPropertyRowMapper(elementType));
    }
    public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, getBeanPropertyRowMapper(elementType));
    }
    public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException {
        return query(sql, args, argTypes, getBeanPropertyRowMapper(elementType));
    }
    public <T> List<T> queryForList(String sql, SqlParameterSource paramSource, Class<T> elementType) throws DataAccessException {
        return query(sql, paramSource, getBeanPropertyRowMapper(elementType));
    }
    public <T> List<T> queryForList(String sql, Map<String, ?> paramMap, Class<T> elementType) throws DataAccessException {
        return queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
    }
    //
    //
    //
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        List<T> results = query(sql, rowMapper);
        return requiredSingleResult(results);
    }
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        List<T> results = query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
        return requiredSingleResult(results);
    }
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
        List<T> results = query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
        return requiredSingleResult(results);
    }
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
        List<T> results = query(sql, args, argTypes, new RowMapperResultSetExtractor<T>(rowMapper, 1));
        return requiredSingleResult(results);
    }
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper) throws DataAccessException {
        List<T> results = query(getPreparedStatementCreator(sql, paramSource), rowMapper);
        return requiredSingleResult(results);
    }
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) throws DataAccessException {
        return queryForObject(sql, new MapSqlParameterSource(paramMap), rowMapper);
    }
    public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, getBeanPropertyRowMapper(requiredType));
    }
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
        return queryForObject(sql, args, getBeanPropertyRowMapper(requiredType));
    }
    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, args, getBeanPropertyRowMapper(requiredType));
    }
    public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, args, argTypes, getBeanPropertyRowMapper(requiredType));
    }
    public <T> T queryForObject(String sql, SqlParameterSource paramSource, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, paramSource, getBeanPropertyRowMapper(requiredType));
    }
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws DataAccessException {
        return queryForObject(sql, paramMap, getBeanPropertyRowMapper(requiredType));
    }
    //
    //
    //
    public long queryForLong(String sql) throws DataAccessException {
        Number number = queryForObject(sql, getSingleColumnRowMapper(Long.class));
        return (number != null ? number.longValue() : 0);
    }
    public long queryForLong(String sql, Object... args) throws DataAccessException {
        Number number = queryForObject(sql, args, getSingleColumnRowMapper(Long.class));
        return (number != null ? number.longValue() : 0);
    }
    public long queryForLong(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        Number number = queryForObject(sql, args, argTypes, getSingleColumnRowMapper(Long.class));
        return (number != null ? number.longValue() : 0);
    }
    public long queryForLong(String sql, SqlParameterSource paramSource) throws DataAccessException {
        Number number = queryForObject(sql, paramSource, getSingleColumnRowMapper(Number.class));
        return (number != null ? number.longValue() : 0);
    }
    public long queryForLong(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForLong(sql, new MapSqlParameterSource(paramMap));
    }
    public int queryForInt(String sql) throws DataAccessException {
        Number number = queryForObject(sql, getSingleColumnRowMapper(Integer.class));
        return (number != null ? number.intValue() : 0);
    }
    public int queryForInt(String sql, Object... args) throws DataAccessException {
        Number number = queryForObject(sql, args, getSingleColumnRowMapper(Integer.class));
        return (number != null ? number.intValue() : 0);
    }
    public int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        Number number = queryForObject(sql, args, argTypes, getSingleColumnRowMapper(Integer.class));
        return (number != null ? number.intValue() : 0);
    }
    public int queryForInt(String sql, SqlParameterSource paramSource) throws DataAccessException {
        Number number = queryForObject(sql, paramSource, getSingleColumnRowMapper(Number.class));
        return (number != null ? number.intValue() : 0);
    }
    public int queryForInt(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForInt(sql, new MapSqlParameterSource(paramMap));
    }
    //
    //
    //
    public Map<String, Object> queryForMap(String sql) throws DataAccessException {
        return queryForObject(sql, getColumnMapRowMapper());
    }
    public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException {
        return queryForObject(sql, args, getColumnMapRowMapper());
    }
    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        return queryForObject(sql, args, argTypes, getColumnMapRowMapper());
    }
    public Map<String, Object> queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return queryForObject(sql, paramSource, getColumnMapRowMapper());
    }
    public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForObject(sql, paramMap, getColumnMapRowMapper());
    }
    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
        return query(sql, getColumnMapRowMapper());
    }
    public List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException {
        return query(sql, args, getColumnMapRowMapper());
    }
    public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        return query(sql, args, argTypes, getColumnMapRowMapper());
    }
    public List<Map<String, Object>> queryForList(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return query(sql, paramSource, getColumnMapRowMapper());
    }
    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForList(sql, new MapSqlParameterSource(paramMap));
    }
    //
    //
    //
    public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
        return query(sql, new SqlRowSetResultSetExtractor());
    }
    public SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException {
        return query(sql, args, new SqlRowSetResultSetExtractor());
    }
    public SqlRowSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        return query(sql, args, argTypes, new SqlRowSetResultSetExtractor());
    }
    public SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return query(getPreparedStatementCreator(sql, paramSource), new SqlRowSetResultSetExtractor());
    }
    public SqlRowSet queryForRowSet(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return queryForRowSet(sql, new MapSqlParameterSource(paramMap));
    }
    //
    //
    //
    /***/
    public int update(final PreparedStatementCreator psc, final PreparedStatementSetter pss) throws DataAccessException {
        Hasor.logDebug("Executing prepared SQL update");
        return execute(psc, new PreparedStatementCallback<Integer>() {
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
                try {
                    if (pss != null)
                        pss.setValues(ps);
                    int rows = ps.executeUpdate();
                    Hasor.logDebug("SQL update affected " + rows + " rows");
                    return rows;
                } finally {
                    if (pss instanceof ParameterDisposer)
                        ((ParameterDisposer) pss).cleanupParameters();
                }
            }
        });
    }
    public int update(PreparedStatementCreator psc) throws DataAccessException {
        return update(psc, (PreparedStatementSetter) null);
    }
    public int update(final String sql) throws DataAccessException {
        Hasor.assertIsNotNull(sql, "SQL must not be null");
        Hasor.logDebug("Executing SQL update [%s]", sql);
        //
        class UpdateStatementCallback implements StatementCallback<Integer>, SqlProvider {
            public Integer doInStatement(Statement stmt) throws SQLException {
                int rows = stmt.executeUpdate(sql);
                Hasor.logDebug("SQL update affected %s rows.", rows);
                return rows;
            }
            public String getSql() {
                return sql;
            }
        }
        return execute(new UpdateStatementCallback());
    }
    public int update(String sql, PreparedStatementSetter pss) throws DataAccessException {
        return update(new SimplePreparedStatementCreator(sql), pss);
    }
    public int update(String sql, Object... args) throws DataAccessException {
        return update(sql, newArgPreparedStatementSetter(args));
    }
    public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        return update(sql, newArgTypePreparedStatementSetter(args, argTypes));
    }
    public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
        return update(getPreparedStatementCreator(sql, paramSource));
    }
    public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
        return update(sql, new MapSqlParameterSource(paramMap));
    }
    //
    //
    //
    public int[] batchUpdate(final String[] sql) throws DataAccessException {
        if (ArrayUtils.isEmpty(sql))
            throw new NullPointerException(sql + "SQL array must not be empty");
        Hasor.logDebug("Executing SQL batch update of %s statements", sql.length);
        //
        class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {
            private String currSql;
            public int[] doInStatement(Statement stmt) throws SQLException, DataAccessException {
                DatabaseMetaData dbmd = stmt.getConnection().getMetaData();
                int[] rowsAffected = new int[sql.length];
                if (dbmd.supportsBatchUpdates()) {
                    /*连接支持批处理*/
                    for (String sqlStmt : sql) {
                        this.currSql = sqlStmt;
                        stmt.addBatch(sqlStmt);
                    }
                    rowsAffected = stmt.executeBatch();
                } else {
                    /*连接不支持批处理*/
                    for (int i = 0; i < sql.length; i++) {
                        this.currSql = sql[i];
                        if (!stmt.execute(sql[i]))
                            rowsAffected[i] = stmt.getUpdateCount();
                        else
                            throw new InvalidDataAccessException("Invalid batch SQL statement: " + sql[i]);
                    }
                }
                return rowsAffected;
            }
            public String getSql() {
                return this.currSql;
            }
        }
        return execute(new BatchUpdateStatementCallback());
    }
    public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws DataAccessException {
        Hasor.logDebug("Executing SQL batch update [%s].", sql);
        return execute(sql, new PreparedStatementCallback<int[]>() {
            public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException {
                try {
                    int batchSize = pss.getBatchSize();
                    InterruptibleBatchPreparedStatementSetter ipss = (pss instanceof InterruptibleBatchPreparedStatementSetter ? (InterruptibleBatchPreparedStatementSetter) pss : null);
                    DatabaseMetaData dbMetaData = ps.getConnection().getMetaData();
                    if (dbMetaData.supportsBatchUpdates()) {
                        for (int i = 0; i < batchSize; i++) {
                            pss.setValues(ps, i);
                            if (ipss != null && ipss.isBatchExhausted(i))
                                break;
                            ps.addBatch();
                        }
                        return ps.executeBatch();
                    } else {
                        List<Integer> rowsAffected = new ArrayList<Integer>();
                        for (int i = 0; i < batchSize; i++) {
                            pss.setValues(ps, i);
                            if (ipss != null && ipss.isBatchExhausted(i))
                                break;
                            rowsAffected.add(ps.executeUpdate());
                        }
                        int[] rowsAffectedArray = new int[rowsAffected.size()];
                        for (int i = 0; i < rowsAffectedArray.length; i++)
                            rowsAffectedArray[i] = rowsAffected.get(i);
                        return rowsAffectedArray;
                    }
                } finally {
                    if (pss instanceof ParameterDisposer)
                        ((ParameterDisposer) pss).cleanupParameters();
                }
            }
        });
    }
    public int[] batchUpdate(String sql, Map<String, ?>[] batchValues) {
        SqlParameterSource[] batchArgs = new SqlParameterSource[batchValues.length];
        int i = 0;
        for (Map<String, ?> values : batchValues) {
            batchArgs[i] = new MapSqlParameterSource(values);
            i++;
        }
        return batchUpdate(sql, batchArgs);
    }
    public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {
        ParsedSql parsedSql = this.getParsedSql(sql);
        return NamedBatchUpdateUtils.executeBatchUpdateWithNamedParameters(parsedSql, batchArgs, this);
    }
    //
    //
    //
    /** Create a new RowMapper for reading columns as key-value pairs. */
    protected RowMapper<Map<String, Object>> getColumnMapRowMapper() {
        return new ColumnMapRowMapper() {
            protected Map<String, Object> createColumnMap(int columnCount) {
                return createResultsMap();
            }
        };
    }
    /** Create a new RowMapper for reading columns as Bean pairs. */
    protected <T> RowMapper<T> getBeanPropertyRowMapper(Class<T> requiredType) {
        Hasor.assertIsNotNull(requiredType != null, "requiredType is null.");
        if (Map.class.isAssignableFrom(requiredType))
            return (RowMapper<T>) getColumnMapRowMapper();
        //
        if (requiredType.isPrimitive() || Number.class.isAssignableFrom(requiredType) || String.class.isAssignableFrom(requiredType))
            return getSingleColumnRowMapper(requiredType);
        //
        return new BeanPropertyRowMapper<T>(requiredType) {
            public boolean isCaseInsensitive() {
                return isResultsCaseInsensitive();
            }
        };
    }
    /** Create a new RowMapper for reading result objects from a single column.*/
    protected <T> RowMapper<T> getSingleColumnRowMapper(Class<T> requiredType) {
        return new SingleColumnRowMapper<T>(requiredType);
    }
    //
    //
    //
    /**创建用于保存结果集的数据Map。*/
    protected Map<String, Object> createResultsMap() {
        if (!isResultsCaseInsensitive())
            return new LinkedCaseInsensitiveMap<Object>();
        else
            return new LinkedHashMap<String, Object>();
    }
    /** Create a new PreparedStatementSetter.*/
    protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
        return new ArgPreparedStatementSetter(args);
    }
    /**Create a new ArgTypePreparedStatementSetter using the args and argTypes passed in.
     * This method allows the creation to be overridden by sub-classes.
     */
    protected PreparedStatementSetter newArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
        return new ArgTypePreparedStatementSetter(args, argTypes);
    }
    /**对Statement的属性进行设置。设置 JDBC Statement 对象的 fetchSize、maxRows、Timeout等参数。*/
    protected void applyStatementSettings(Statement stmt) throws SQLException {
        int fetchSize = getFetchSize();
        if (fetchSize > 0)
            stmt.setFetchSize(fetchSize);
        int maxRows = getMaxRows();
        if (maxRows > 0)
            stmt.setMaxRows(maxRows);
        int timeout = this.getQueryTimeout();
        if (timeout > 0)
            stmt.setQueryTimeout(timeout);
    }
    /**
     * Build a PreparedStatementCreator based on the given SQL and named parameters.
     * <p>Note: Not used for the <code>update</code> variant with generated key handling.
     * @param sql SQL to execute
     * @param paramSource container of arguments to bind
     * @return the corresponding PreparedStatementCreator
     */
    protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
        ParsedSql parsedSql = getParsedSql(sql);
        String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
        Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
        int[] paramTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, paramTypes);
        return pscf.newPreparedStatementCreator(params);
    }
    //
    /**处理潜在的 SQL 警告。当要求不忽略 SQL 警告时，检测到 SQL 警告抛出 SQL 异常。*/
    private void handleWarnings(Statement stmt) throws SQLException {
        if (isIgnoreWarnings()) {
            if (Hasor.isDebugLogger()) {
                SQLWarning warningToLog = stmt.getWarnings();
                while (warningToLog != null) {
                    Hasor.logDebug("SQLWarning ignored: SQL state '%s', error code '%s', message [%s].", warningToLog.getSQLState(), warningToLog.getErrorCode(), warningToLog.getMessage());
                    warningToLog = warningToLog.getNextWarning();
                }
            }
        } else {
            SQLWarning warning = stmt.getWarnings();
            if (warning != null)
                throw new SQLWarningException("Warning not ignored", warning);
        }
    }
    private static String getSql(Object sqlProvider) {
        if (sqlProvider instanceof SqlProvider)
            return ((SqlProvider) sqlProvider).getSql();
        else
            return null;
    }
    private ParsedSql getParsedSql(String sql) {
        if (getCacheLimit() <= 0)
            return NamedParameterUtils.parseSqlStatement(sql);
        synchronized (this.parsedSqlCache) {
            ParsedSql parsedSql = this.parsedSqlCache.get(sql);
            if (parsedSql == null) {
                parsedSql = NamedParameterUtils.parseSqlStatement(sql);
                this.parsedSqlCache.put(sql, parsedSql);
            }
            return parsedSql;
        }
    }
    //
    /**至返回结果集中的一条数据。*/
    private static <T> T requiredSingleResult(Collection<T> results) throws InvalidDataAccessException {
        int size = (results != null ? results.size() : 0);
        if (size == 0)
            throw new InvalidDataAccessException("Empty Result");
        if (results.size() > 1)
            throw new InvalidDataAccessException("Incorrect column count: expected " + 1 + ", actual " + size);
        return results.iterator().next();
    }
    /**获取与本地线程绑定的数据库连接，JDBC 框架会维护这个连接的事务。开发者不必关心该连接的事务管理，以及资源释放操作。*/
    private Connection newProxyConnection(Connection conn) {
        Hasor.assertIsNotNull(conn, "Connection is null.");
        CloseSuppressingInvocationHandler handler = new CloseSuppressingInvocationHandler(conn);
        return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class[] { ConnectionProxy.class }, handler);
    }
    /**Connection 接口代理，目的是为了控制一些方法的调用。同时进行一些特殊类型的处理。*/
    private class CloseSuppressingInvocationHandler implements InvocationHandler {
        private final Connection target;
        public CloseSuppressingInvocationHandler(Connection target) {
            this.target = target;
        }
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Invocation on ConnectionProxy interface coming in...
            if (method.getName().equals("getTargetConnection")) {
                // Handle getTargetConnection method: return underlying Connection.
                return this.target;
            } else if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if (method.getName().equals("hashCode")) {
                // Use hashCode of PersistenceManager proxy.
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("close")) {
                return null;
            }
            // Invoke method on target Connection.
            try {
                Object retVal = method.invoke(this.target, args);
                // If return value is a JDBC Statement, apply statement settings (fetch size, max rows, transaction timeout).
                if (retVal instanceof Statement)
                    applyStatementSettings(((Statement) retVal));
                return retVal;
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
    /**接口 {@link PreparedStatementCreator} 的简单实现，目的是根据 SQL 语句创建 {@link PreparedStatement}对象。*/
    private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {
        private final String sql;
        public SimplePreparedStatementCreator(String sql) {
            Hasor.assertIsNotNull(sql, "SQL must not be null");
            this.sql = sql;
        }
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            return con.prepareStatement(this.sql);
        }
        public String getSql() {
            return this.sql;
        }
    }
    /**接口 {@link CallableStatementCreator} 的简单实现，目的是根据 SQL 语句创建 {@link CallableStatement}对象。*/
    private static class SimpleCallableStatementCreator implements CallableStatementCreator, SqlProvider {
        private final String callString;
        public SimpleCallableStatementCreator(String callString) {
            Hasor.assertIsNotNull(callString, "Call string must not be null");
            this.callString = callString;
        }
        public CallableStatement createCallableStatement(Connection con) throws SQLException {
            return con.prepareCall(this.callString);
        }
        public String getSql() {
            return this.callString;
        }
    }
    /**使用 {@link RowCallbackHandler} 类型循环处理每一行记录的适配器*/
    private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Object> {
        private final RowCallbackHandler rch;
        public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch) {
            this.rch = rch;
        }
        public Object extractData(ResultSet rs) throws SQLException {
            while (rs.next()) {
                this.rch.processRow(rs);
            }
            return null;
        }
    }
}