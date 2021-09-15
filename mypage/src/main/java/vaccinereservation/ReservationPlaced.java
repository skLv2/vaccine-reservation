package vaccinereservation;

public class ReservationPlaced extends AbstractEvent {

    private Long id;
    private String reservationid;
    private String customerid;
    private String hospitalid;
    private String date;
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

    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }
    public String getHospitalid() {
        return hospitalid;
    }

    public void setHospitalid(String hospitalid) {
        this.hospitalid = hospitalid;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}