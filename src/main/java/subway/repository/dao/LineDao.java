package subway.repository.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import subway.entity.LineEntity;

@Repository
public class LineDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertAction;

    private RowMapper<LineEntity> rowMapper = (rs, rowNum) ->
            new LineEntity(
                    rs.getLong("id"),
                    rs.getString("name")
            );

    public LineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertAction = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("line")
                .usingGeneratedKeyColumns("id");
    }

    public LineEntity insert(LineEntity lineEntity) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", lineEntity.getName());

        Long lineId = insertAction.executeAndReturnKey(params).longValue();
        return new LineEntity(lineId, lineEntity.getName());
    }

    public List<LineEntity> findAll() {
        String sql = "select id, name from LINE";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public LineEntity findById(Long id) {
        String sql = "select id, name from LINE WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public void update(LineEntity lineEntity) {
        String sql = "update LINE set name = ? where id = ?";
        jdbcTemplate.update(sql, new Object[]{lineEntity.getName(), lineEntity.getId()});
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("delete from Line where id = ?", id);
    }

    public boolean existsByName(String name) {
        String sql = "select exists (select * from line where name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }
}
