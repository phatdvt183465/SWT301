package swp391.com.swp391.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swp391.com.swp391.dto.request.StaffCreationRequest;
import swp391.com.swp391.dto.request.StaffUpdatePasswordRequest;
import swp391.com.swp391.dto.request.StaffUpdateRequest;
import swp391.com.swp391.entity.Staff;
import swp391.com.swp391.exception.AppException;
import swp391.com.swp391.exception.ErrorCode;
import swp391.com.swp391.mapper.StaffMapper;
import swp391.com.swp391.repository.StaffRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StaffService {
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    StaffMapper staffMapper;

    //hàm này dùng để tạo ra 1 staff mới
    public Staff createStaff(StaffCreationRequest request){
        if (staffRepository.existsByUsername(request.getUsername())) //.getRole()
            throw new AppException(ErrorCode.USER_EXISTED);
        if (staffRepository.existsByMail(request.getMail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        if (!request.getPassword().matches(request.getConfirm_password()))
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        Staff staff = new Staff();
        staff.setUsername(request.getUsername());
        staff.setPassword(request.getPassword());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setMail(request.getMail());
        staff.setRole(request.getRole());
        return staffRepository.save(staff);
    }

    //hàm này dùng để xem danh sách tất cả Staff
    public List<Staff> viewAllStaffLists(){
        return staffRepository.findAll();
    }

    //hàm này dùng để xem thông tin của nhân viên dựa trên staffId
    public Staff viewStaffById(int staffId){
        return staffRepository.findById(String.valueOf(staffId)).
            orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); //lambda expression
    }

    //hàm này dùng để cập nhật thông tin của staff dựa trên staffId
    public Staff updateStaff(int staffId, StaffUpdateRequest request){
        Staff staff = viewStaffById(staffId);
        staff.setMail(request.getMail());
        staff.setFullName(request.getFullName());
        staff.setAddress(request.getAddress());
        staff.setPhone(request.getPhone());
        staff.setRole(request.getRole());
        return staffRepository.save(staff);
    }

    public int getStaffIdByUsername(String username){
        return staffRepository.findByUsername(username).map(Staff::getStaffId).orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    public Staff getStaffByMail(String mail) {
        return staffRepository.findByMail(mail).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    //hàm này dùng để thay đổi password của staff dựa trên mail
    public Staff updateStaffPasswordByMail(String mail, StaffUpdatePasswordRequest request) {
        Staff staff = getStaffByMail(mail);
        if(staff.getPassword().isEmpty()){
            throw new AppException(ErrorCode.LOGIN_GG_NOT_PASSWORD);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        return staffRepository.save(staff);
    }

    //hàm này dùng để xóa Staff dựa trên staffId
    public void deleteStaff(int staffId){
        staffRepository.deleteById(String.valueOf(staffId));
    }

    //Hàm này dùng để lấy Role dựa trên username đó
    public String getRoleByUsername(String username) {
        var staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return staff.getRole();
    }
}
