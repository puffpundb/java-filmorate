package ru.yandex.practicum.filmorate.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MpaRatingRowMapper implements RowMapper<MpaRating> {
	@Override
	public MpaRating mapRow(ResultSet rs, int rowNum) throws SQLException {
		MpaRating mpaRating = new MpaRating();
		mpaRating.setId(rs.getInt("id"));
		mpaRating.setRating(rs.getString("rating"));

		return mpaRating;
	}
}
