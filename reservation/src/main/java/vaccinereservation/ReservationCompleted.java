package vaccinereservation;

public class ReservationCompleted extends AbstractEvent {

    private Long id;
    private String reservationid;
    private String vaccinetype;
    private Integer qty;
    private String shelflife;
    private String productiondate;

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
    public String getVaccinetype() {
        return vaccinetype;
    }

    public void setVaccinetype(String vaccinetype) {
        this.vaccinetype = vaccinetype;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getShelflife() {
        return shelflife;
    }

    public void setShelflife(String shelflife) {
        this.shelflife = shelflife;
    }
    public String getProductiondate() {
        return productiondate;
    }

    public void setProductiondate(String productiondate) {
        this.productiondate = productiondate;
    }
}