package vaccinereservation.external;

public class Approval {

    private Long id;
    private String reservationid;
    private String status;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getReservationid() {
        return reservationid;
    }
    public void setReservationid(String reservationid) {
        this.reservationid = reservationid;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
