package cwms.radar.data.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A representation of a CWMS office")
@XmlRootElement(name="office")
@XmlAccessorType(XmlAccessType.FIELD)
public class Office implements CwmsDTO{
    private static final HashMap<String,String> office_types = new HashMap<String,String>(){
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("UNK","unknown");
            put("HQ","corps headquarters");
            put("MSC","division headquarters");
            put("MSCR","division regional");
            put("DIS","district");
            put("FOA","field operating activity");
        }
    };

    private String name;
    @XmlElement(name="long-name")
    private String longName;
    @Schema(allowableValues = {"unknown","corps headquarters","division headquarters","division regional","district","filed operating activity"})
    private String type;
    @XmlElement(name="reports-to")
    @Schema(description = "Reference to another office, like a division, that this office reports to.")
    private String reportsTo;

    public Office(){}

    public Office(ResultSet rs ) throws SQLException {
        name = rs.getString("office_id");
        longName = rs.getString("long_name");
        type = office_types.get(rs.getString("office_type"));
        reportsTo = rs.getString("report_to_office_id");
    }

    public String getName(){ return name; }
    public String getLongName(){ return longName; }
    public String getType(){ return type; }
    public String getReportsTo(){ return reportsTo; }
}
