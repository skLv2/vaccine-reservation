package vaccinereservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Approval_table")
public class Approval {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String reservationid;
    private String status;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 예약 승인 요청 ============== ");

        ApprovalFinished approvalFinished = new ApprovalFinished();
        BeanUtils.copyProperties(this, approvalFinished);
        approvalFinished.publishAfterCommit();

    }

    @PrePersist
    public void onPrePersist(){
        System.out.println(" ============== 예약 승인 전 ============== ");
        status = "APV_COMPLETED";
    }

    @PostUpdate
    public void onPostUpdate(){
        ApprovalDenied approvalDenied = new ApprovalDenied();
        BeanUtils.copyProperties(this, approvalDenied);
        approvalDenied.publishAfterCommit();

    }

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