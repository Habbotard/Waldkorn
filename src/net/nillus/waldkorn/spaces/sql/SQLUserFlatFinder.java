package net.nillus.waldkorn.spaces.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import net.nillus.waldkorn.spaces.Space;
import net.nillus.waldkorn.spaces.UserFlatFinder;


import com.blunk.storage.sql.SQLDataQuery;

public class SQLUserFlatFinder extends UserFlatFinder implements SQLDataQuery
{
	@Override
	public void execute(Connection conn) throws SQLException
	{
		
	}
	
	@Override
	public Vector<Space> query(Connection conn) throws SQLException
	{
		// Prepare the query
		PreparedStatement query;
		if (super.searchByUser) // Find flats of one user
		{
			query = conn.prepareStatement("SELECT spaces.*,users.name AS owner FROM spaces JOIN users ON spaces.ownerid = users.id WHERE ownerid = ?;");
			query.setInt(1, super.userID);
		}
		else if (super.searchBusy) // Retrieve main index, sort on busyness
		{
			query = conn.prepareStatement("SELECT spaces.*,users.name AS owner FROM spaces JOIN users ON spaces.ownerid = users.id ORDER BY users_now DESC LIMIT ?,?;");
			query.setInt(1, super.start);
			query.setInt(2, super.stop);
		}
		else if(super.searchFavorites)
		{
			query = conn.prepareStatement("SELECT spaces.*,users.name AS owner FROM spaces JOIN users ON spaces.ownerid = users.id WHERE spaces.id IN(SELECT spaceid FROM users_favoriteflats WHERE userid = ?);");
			query.setInt(1, super.userID);
		}
		else
		{
			// Search on criteria
			query = conn.prepareStatement("SELECT spaces.*,users.name AS owner FROM spaces JOIN users ON spaces.ownerid = users.id WHERE spaces.name LIKE ? OR users.name = ? LIMIT 35;");
			query.setString(1, "%" + this.criteria + "%");
			query.setString(2, this.criteria);
		}
		
		// Execute the query
		ResultSet result = query.executeQuery();
		
		// Unwrap and parse the results
		Vector<Space> found = new Vector<Space>(5);
		while (result.next())
		{
			Space space = new SQLSpace();
			if(SQLSpace.parseFromResultSet(space, result))
			{
				found.add(space);
			}
		}
		
		query.close();
		
		// Return the result bucket
		return found;
	}
}
