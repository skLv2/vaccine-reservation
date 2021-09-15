package vaccinereservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerid;
    private String hospitalid;
    private String date;
    private String status;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 백신 예약 요청 ============== ");
        ReservationPlaced reservationPlaced = new ReservationPlaced();
        BeanUtils.copyProperties(this, reservationPlaced);
        reservationPlaced.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        vaccinereservation.external.Approval approval = new vaccinereservation.external.Approval();
        // mappings goes here
        /* 승인(approval) 동기 호출 진행 */
        /* 승인 진행 가능 여부 확인 후 백신매핑 */
        if(this.getStatus().equals("RSV_REQUESTED")){

            approval.setReservationid(Long.toString(this.getId()));
            approval.setStatus("APV_REQUESTED");
        }

        ReservationApplication.applicationContext.getBean(vaccinereservation.external.ApprovalService.class)
            .requestapproval(approval);

    }
    
    @PrePersist
    public void onPrePersist(){
        System.out.println(" ============== 백신 예약 요청 전 ============== ");
        status = "RSV_REQUESTED";
    }

    @PostUpdate
    public void onPostUpdate(){

        System.out.println(" ============== 백신 취소 요청 ============== ");

        if(this.getStatus().equals("CANCEL_REQUESTED") ){
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            reservationCanceled.publishAfterCommit();
        }

        

    }
    @PreRemove
    public void onPreRemove(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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