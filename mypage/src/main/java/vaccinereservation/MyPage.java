package vaccinereservation;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;
        private String reservationid;
        private String customerid;
        private String hospitalid;
        private String date;
        private String vaccinetype;
        private String status;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getreservationid() {
            return reservationid;
        }

        public void setreservationid(String reservationid) {
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
        public String getVaccinetype() {
            return vaccinetype;
        }

        public void setVaccinetype(String vaccinetype) {
            this.vaccinetype = vaccinetype;
        }
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

}
