package cwms.cda.data.dto;

import cwms.cda.api.errors.FieldException;
import cwms.cda.data.dto.catalog.CatalogEntry;
import cwms.cda.data.dto.catalog.LocationCatalogEntry;
import cwms.cda.data.dto.catalog.TimeseriesCatalogEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "catalog")
@XmlAccessorType(XmlAccessType.FIELD)
public class Catalog extends CwmsDTOPaginated {
    @Schema(
            oneOf = {
                    LocationCatalogEntry.class,
                    TimeseriesCatalogEntry.class
            }
    )
    @XmlElementWrapper(name = "entries")
    @XmlElement(name = "entry")
    private List<? extends CatalogEntry> entries;

    @SuppressWarnings("unused") // required so JAXB can initialize and marshal
    private Catalog() {
    }

    public Catalog(String page, int total, int pageSize, List<? extends CatalogEntry> entries) {
        this(page, total, pageSize, entries, null, null, null, null, null, null, null);
    }

    @SuppressWarnings("java:S107") // This just has this many parameters.
    public Catalog(String page, int total, int pageSize, List<? extends CatalogEntry> entries,
                   String office,
                   String idLike, String locCategoryLike, String locGroupLike,
                   String tsCategoryLike,
                   String tsGroupLike) {
        this(page, total, pageSize, entries, office, idLike, locCategoryLike, locGroupLike, tsCategoryLike, tsGroupLike, null);
    }

    @SuppressWarnings("java:S107") // This just has this many parameters.
    public Catalog(String page, int total, int pageSize, List<? extends CatalogEntry> entries,
                   String office,
                   String idLike, String locCategoryLike, String locGroupLike,
                   String tsCategoryLike,
                   String tsGroupLike, String boundingOfficeLike) {
        super(page, pageSize, total);

        Objects.requireNonNull(entries, "List of catalog entries must be a valid list, even if empty");
        this.entries = entries;
        if (entries.size() == pageSize) {
            nextPage = encodeCursor(new CatalogPage(
                            entries.get(entries.size() - 1).getCursor(),
                            office,
                            idLike,
                            locCategoryLike,
                            locGroupLike,
                            tsCategoryLike,
                            tsGroupLike,
                            boundingOfficeLike
                    ).toString(),
                    pageSize, total);

        } else {
            nextPage = null;
        }
    }

    /**
     * @return List<? extends CatalogEntry> return the entries
     */
    public List<? extends CatalogEntry> getEntries() {
        return entries;
    }

    @Override
    public void validate() throws FieldException{
        // catalogs are never accepted as user input
    }

    public static class CatalogPage {
        private final String curOffice;
        private final String cursorId;
        private final String searchOffice;
        private final String idLike;
        private final String locCategoryLike;
        private final String locGroupLike;
        private final String tsCategoryLike;
        private final String tsGroupLike;
        private final String boundingOfficeLike;
        private int total;
        private int pageSize;

        public CatalogPage(String page) {
            String[] parts = CwmsDTOPaginated.decodeCursor(page, CwmsDTOPaginated.delimiter);

            if (parts.length != 10) {
                throw new IllegalArgumentException("Invalid Catalog Page Provided, please verify "
                        + "you are using a page variable from the catalog endpoint");
            }
            String[] idParts = parts[0].split("/");
            curOffice = idParts[0];
            cursorId = idParts[1];
            searchOffice = nullOrVal(parts[1]);
            idLike = nullOrVal(parts[2]);
            locCategoryLike = nullOrVal(parts[3]);
            locGroupLike = nullOrVal(parts[4]);
            tsCategoryLike = nullOrVal(parts[5]);
            tsGroupLike = nullOrVal(parts[6]);
            boundingOfficeLike = nullOrVal(parts[7]);
            total = Integer.parseInt(parts[8]);
            pageSize = Integer.parseInt(parts[9]);
        }

        public CatalogPage(String curElement, String office, String idLike,
                           String locCategoryLike, String locGroupLike,
                           String tsCategoryLike, String tsGroupLike, String boundingLike
        ) {

            String[] parts = curElement.split("/");
            this.curOffice = parts[0];
            this.cursorId = parts[1];
            this.searchOffice = office;
            this.idLike = idLike;
            this.locCategoryLike = locCategoryLike;
            this.locGroupLike = locGroupLike;
            this.tsCategoryLike = tsCategoryLike;
            this.tsGroupLike = tsGroupLike;
            this.boundingOfficeLike = boundingLike;
        }

        private String nullOrVal(String val) {
            if (val == null || val.equalsIgnoreCase("null")) {
                return null;
            } else {
                return val;
            }
        }

        public String getSearchOffice() {
            return searchOffice;
        }

        public String getCurOffice() {
            return curOffice;
        }

        public String getCursorId() {
            return cursorId;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getTotal() {
            return total;
        }

        public String getIdLike() {
            return idLike;
        }

        public String getLocCategoryLike() {
            return locCategoryLike;
        }

        public String getLocGroupLike() {
            return locGroupLike;
        }

        public String getTsCategoryLike() {
            return tsCategoryLike;
        }

        public String getTsGroupLike() {
            return tsGroupLike;
        }

        public String getBoundingOfficeLike() {
            return boundingOfficeLike;
        }

        @Override
        public String toString() {
            return curOffice + "/" + cursorId
                    + CwmsDTOPaginated.delimiter + searchOffice
                    + CwmsDTOPaginated.delimiter + idLike
                    + CwmsDTOPaginated.delimiter + locCategoryLike
                    + CwmsDTOPaginated.delimiter + locGroupLike
                    + CwmsDTOPaginated.delimiter + tsCategoryLike
                    + CwmsDTOPaginated.delimiter + tsGroupLike
                    + CwmsDTOPaginated.delimiter + boundingOfficeLike;
        }
    }
}
