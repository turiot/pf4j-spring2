package demo2.plugin2;

import demo2.api.Interface;
import demo2.api.Model;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Repository
public class Dao implements Interface
	, InitializingBean
{

    private static final Logger log = LoggerFactory.getLogger(Dao.class);

    private JdbcTemplate jdbcTemplate;

	public Dao() { // TODO conf
		System.out.println("CONSTRUCTOR===================");		
//		log.info("constructor---------------start");
//		var dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("org.h2.Driver");
//		dataSource.setUrl("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
//		jdbcTemplate = new JdbcTemplate(dataSource);
//		jdbcTemplate.execute("create table tbl(key varchar2(5), value varchar2(100)");
//		log.info("constructor---------------end");
	}
    @PostConstruct
    public void init() {
		System.out.println("INIT===================");		
		log.info("init---------------start");
		try {
			DriverManager.registerDriver(new org.h2.Driver());
		}
		catch (SQLException ex) {
			log.error("***************", ex);
		}
		var dataSource = new SimpleDriverDataSource();
		dataSource.setDriver(new org.h2.Driver());
		dataSource.setUrl("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.execute("create table tbl(key varchar2(5), value varchar2(100))");
		log.info("init---------------end");
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}
	
	
	@Override
	public void save(Model model) {
		log.info("save---------------"+model.key+" "+model.value);
		jdbcTemplate.execute("insert into tbl values('"+model.key+"','"+model.value+"')");
	}

	@Override
	public Model get(String key) {
		return jdbcTemplate.queryForObject("select * from tbl where key='"+key+"'", (rs, i) -> {
			var model = new Model();
			model.key=rs.getString(1);
			model.value=rs.getString(2);
			return model;
		});
	}

}
