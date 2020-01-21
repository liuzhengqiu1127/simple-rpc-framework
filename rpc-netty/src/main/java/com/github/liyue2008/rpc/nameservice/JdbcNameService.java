package com.github.liyue2008.rpc.nameservice;

import com.github.liyue2008.rpc.NameService;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class JdbcNameService implements NameService, Closeable {
    private static final Collection<String> schemes = Collections.singleton("jdbc");
    private static final String DDL_SQL_FILE_NAME = "ddl";
    private static final String LOOKUP_SERVICE_SQL_FILE_NAME = "lookup-service";
    private static final String ADD_SERVICE_SQL_FILE_NAME = "add-service";
    private Connection connection = null;
    private String subprotocol = null;
    @Override
    public Collection<String> supportedSchemes() {
        return this.schemes;
    }
    @Override
    public void connect(URI nameServiceUri) {
        close();
        subprotocol = nameServiceUri.toString().split(":")[1];
        log.info("Database: {}",subprotocol);
        String username = System.getProperty("nameservice.jdbc.username");
        String password = System.getProperty("nameservice.jdbc.password");
        log.info("Connecting to database: {}...",nameServiceUri);
        try{
            if (null == username){
                connection = DriverManager.getConnection(nameServiceUri.toString());
            }else {
                connection = DriverManager.getConnection(nameServiceUri.toString(),username,password);
            }
            log.info("Maybe execute ddl to init database...");
            maybeExecuteDDL(connection);
            log.info("Database connected.");
        }catch (SQLException | IOException e){
            log.error("Exception: ",e);
            throw new RuntimeException(e);
        }
    }
    private void maybeExecuteDDL(Connection connection) throws SQLException, IOException {
        try(Statement statement = connection.createStatement()){
            String ddlSqlString = readSql(DDL_SQL_FILE_NAME);
            statement.execute(ddlSqlString);
        }
    }
    private String readSql(String filename) throws IOException {
        String ddlFile = toFileName(filename);
        try(InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(ddlFile)){
            if (null != inputStream) return inputStreamToString(inputStream);
            else throw new IOException(ddlFile + " not found in classpath!");
        }
    }
    private String toFileName(String filename){
        return filename + "." + subprotocol + ".sql";
    }
    private String inputStreamToString(InputStream inputStream) throws IOException{
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void registerService(String serviceName, URI uri) throws IOException {
        String sql = readSql(ADD_SERVICE_SQL_FILE_NAME);
        try(PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1,serviceName);
            statement.setString(2,uri.toString());
            statement.executeUpdate();
        }catch (SQLException e){
            log.error("Exception: ",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        try(PreparedStatement statement = connection.prepareStatement(readSql(LOOKUP_SERVICE_SQL_FILE_NAME))){
            statement.setString(1,serviceName);
            ResultSet resultSet = statement.executeQuery();
            List<URI> uriList = new ArrayList<>();
            while (resultSet.next()){
                uriList.add(URI.create(resultSet.getString(1)));
            }
            return uriList.get(ThreadLocalRandom.current().nextInt(uriList.size()));
        }catch (SQLException e){
            log.error("Exception: ",e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close(){
        try {
            if (null != connection) connection.close();
        }catch (SQLException e){
            log.error("Close exception: ",e);
        }
    }
}
