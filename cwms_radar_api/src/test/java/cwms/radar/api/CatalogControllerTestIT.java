package cwms.radar.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isNotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cwms.radar.data.dto.Location;
import cwms.radar.formatters.Formats;
import fixtures.RadarApiSetupCallback;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;
import io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.response.Response;
import mil.army.usace.hec.test.database.CwmsDatabaseContainer;

import static org.hamcrest.Matchers.*;

@Tag("integration")
@ExtendWith(RadarApiSetupCallback.class)
public class CatalogControllerTestIT {

    public static ArrayList<Location> locationsCreated = new ArrayList<>();

    public static String createLocationQuery = null;
    public static String deleteLocationQuery = null;
    public static String createTimeseriesQuery = null;

    @BeforeAll
    public static void setup_data() throws Exception {
        createLocationQuery = IOUtils.toString(
                                TimeseriesControllerTestIT.class
                                    .getClassLoader()
                                    .getResourceAsStream("cwms/radar/data/sql_templates/create_location.sql"),"UTF-8"
                            );
        createTimeseriesQuery = IOUtils.toString(
                                TimeseriesControllerTestIT.class
                                    .getClassLoader()
                                    .getResourceAsStream("cwms/radar/data/sql_templates/create_timeseries.sql"),"UTF-8"
                            );
        deleteLocationQuery = IOUtils.toString(
                                TimeseriesControllerTestIT.class
                                    .getClassLoader()
                                    .getResourceAsStream("cwms/radar/data/sql_templates/delete_location.sql"),"UTF-8"
                            );
        createLocation("Alder Springs",true,"SPK");
        createLocation("Wet Meadows",true,"SPK");
        createLocation("Pine Flat-Outflow",true,"SPK");
        createTimeseries("SPK","Alder Springs.Precip-Cumulative.Inst.15Minutes.0.raw-radar");
        createTimeseries("SPK","Alder Springs.Precip-INC.Total.15Minutes.15Minutes.calc-radar");
        createTimeseries("SPK","Pine Flat-Outflow.Stage.Inst.15Minutes.0.raw-radar");
        createTimeseries("SPK","Wet Meadows.Depth-SWE.Inst.15Minutes.0.raw-radar");
/**

    -- add a timeseries alias
    cwms_ts.store_ts_category('Test Category', 'For Testing', 'F', 'T', 'SPK');
    cwms_ts.store_ts_group('Test Category','Test Group','For testing','F','T',NULL,NULL,'SPK');
    cwms_ts.assign_ts_group(p_ts_category_id=>'Test Category',  p_ts_group_id=>'Test Group', p_ts_id=>'Alder Springs.Precip-Cumulative.Inst.15Minutes.0.raw-radar', p_ts_attribute=>0,p_ts_alias_id=>'Alder Springs 15 Minute Rain Alias-radar',p_ref_ts_id=>NULL,p_db_office_id=>'SPK');
 */

    }

    @AfterAll
    public static void remove_data() {
        locationsCreated.forEach(location -> {
            try {
                CwmsDatabaseContainer<?> db = RadarApiSetupCallback.getDatabaseLink();
                db.connection((c)-> {            
                    try(PreparedStatement stmt = c.prepareStatement(deleteLocationQuery);) {
                        stmt.setString(1,location.getName());                
                        stmt.setString(2,location.getOfficeId());
                        stmt.execute();
                    } catch (SQLException ex) {
                        throw new RuntimeException("Unable to delete location",ex);
                    }
                });    
            } catch(SQLException ex) {
                throw new RuntimeException("Unable to delete location",ex);
            }
        
        });
    }

    private static void createLocation(String location, boolean active, String office) throws SQLException {
        CwmsDatabaseContainer<?> db = RadarApiSetupCallback.getDatabaseLink();
        Location loc = new Location.Builder(location,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            office)
                                    .withActive(active)
                                    .build();
        locationsCreated.add(loc);
        db.connection((c)-> {
            try(PreparedStatement stmt = c.prepareStatement(createLocationQuery);) {
                stmt.setString(1,location);
                stmt.setString(2,active ? "T" : "F");
                stmt.setString(3,office);
                stmt.execute();
            } catch (SQLException ex) {
                throw new RuntimeException("Unable to create location",ex);
            }
        },db.getPdUser());
        
    }

    private static void createTimeseries(String office, String timeseries) throws SQLException {
        CwmsDatabaseContainer<?> db = RadarApiSetupCallback.getDatabaseLink();
        db.connection((c)-> {
            try(PreparedStatement stmt = c.prepareStatement(createTimeseriesQuery);) {
                stmt.setString(1,office);
                stmt.setString(2,timeseries);
                stmt.execute();
            } catch (SQLException ex) {
                throw new RuntimeException("Unable to create location",ex);
            }
        },db.getPdUser());
        
    }

    @Test
    public void test_no_aliased_results_returned(){
        given().accept(Formats.JSONV2)
            .queryParam("office", "SPK")
            .queryParam("like",".*-radar$")
        .get("/catalog/TIMESERIES").then().assertThat()
            .statusCode(is(200))
            .body("$",hasKey("total"))
            .body("total",is(4))
            .body("entries.size()",is(4));
    }


    @Test
    public void test_queries_are_case_insensitive(){
        given().accept("application/json;version=2")
        .queryParam("office", "SPK")
        .queryParam("like","alder spRINgs.*-RADAR$")
        .get("/catalog/TIMESERIES").then().assertThat()
        .statusCode(is(200))
        .body("$",hasKey("total"))
        .body("total",is(2))
        .body("entries.size()",is(2));
    }

    @Test
    public void test_all_office_pagination_works() {
        
        assertTimeout(Duration.ofMinutes(5), () -> {
            final int pageSize = 500;
            Response initialResponse = 
                given()
                    .accept(Formats.JSONV2)
                    .queryParam("page-size",pageSize)
                    .get("/catalog/TIMESERIES")
                .then()
                    .assertThat()
                    .statusCode(is(200))
                    .body("$",hasKey("total"))
                    .body("$",hasKey("next-page"))
                    .body("page-size",is(pageSize))
                    .body("entries.size()",is(pageSize))
                    .extract()
                        .response();
            
            String nextPage = initialResponse.path("next-page");
            
            final int total = initialResponse.path("total");
            int totalRetrieved = initialResponse.path("entries.size()");

            String lastRowPreviousPage = initialResponse.path("entries.last().name");
            do {
                Response pageN = given().accept(Formats.JSONV2)
                .queryParam("page",nextPage)
                .get("/catalog/TIMESERIES")
                .then()
                    .assertThat()
                    .statusCode(is(200))
                    .body("$",hasKey("total"))
                    //.body("$",hasKey("next-page"))                    
                    .body("page-size",is(pageSize))
                    .body("page",equalTo(nextPage))
                    //.body("next-page",not(equalTo(nextPage)))
                    .body("entries[0].name",not(equalTo(lastRowPreviousPage)))
                    .extract().response();
                    ;

                nextPage = pageN.path("next-page");
                
                lastRowPreviousPage = pageN.path("entries.last().name");
                int pageTotal = pageN.path("entries.size()");
                totalRetrieved += pageTotal;
                /*if( nextPage == null && totalRetrieved < total) {
                    fail("Pagination not complete, system returned 'last page' before all values retrieved.");
                }*/
            } while( nextPage != null );
            assertEquals(total,totalRetrieved, "Initial count and retrieval do not match");
        }, "Catalog retrieval got stuck; possibly in endless loop");
        
        
    }
}
