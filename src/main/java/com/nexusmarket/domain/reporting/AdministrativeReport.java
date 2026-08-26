package com.nexusmarket.domain.reporting;

/**
 * Administrative report for consultation.
 *
 * <p>EXPLICIT source: OBJ-12 "Consolidar información administrativa para
 * consulta"; the Supervisor profile is defined for consultation and
 * operational monitoring.</p>
 *
 * <p>PENDING: the content and scope of the reports are not defined in the
 * specification; no attributes are invented.</p>
 *
 * <p>Traceability: OBJ-12.</p>
 */
public class AdministrativeReport {

    /** Unique identifier of the report. */
    private Long id;

    public AdministrativeReport() {
    }

    public AdministrativeReport(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
