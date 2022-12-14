package com.example.authenticationService.services.impl;

import com.example.authenticationService.dtos.BaseResponse;
import com.example.authenticationService.services.AdminService;
import com.example.authenticationService.services.GenerateResetPassCode;
import com.example.authenticationService.dtos.EmailDetails;
import com.example.authenticationService.dtos.UpdatePassword;
import com.example.authenticationService.model.AdminDetails;
import com.example.authenticationService.repository.AdminDetailsRepository;
import com.example.authenticationService.services.FetchInfoService;
import com.example.authenticationService.services.RegisterService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

import static com.example.authenticationService.Utils.Constants.*;
import static com.example.authenticationService.Utils.Urls.MAIL_URL;

@Service
public class AdminServiceImpl implements RegisterService<AdminDetails>, FetchInfoService<AdminDetails, Integer>, AdminService {
    @Autowired
    AdminDetailsRepository adminDetailsRepository;
    @Autowired
    GenerateResetPassCode generateResetPassCode;
    @Autowired
    RestTemplate restTemplate;

    private Map<String, String> generatedCode = new HashMap<>();

    @Override
    public AdminDetails save(AdminDetails adminDetails) {
        Optional<AdminDetails> optionalAdminDetails = adminDetailsRepository.findByEmail(adminDetails.getEmail());
        if(optionalAdminDetails.isPresent())
        {
            return null;
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String password = bCryptPasswordEncoder.encode(adminDetails.getPassword());
        adminDetails.setPassword(password);
        return adminDetailsRepository.save(adminDetails);
    }

    @Override
    public BaseResponse<String> sendCodeToMail(Integer id) {
        String response = "";
        String email="";
        EmailDetails emailDetails = new EmailDetails();
        boolean hasRights = false;
        Optional<AdminDetails> optionalAdminDetails = adminDetailsRepository.findById(id);
        if (optionalAdminDetails.isPresent()) {
            hasRights = optionalAdminDetails.get().isAuthority();
            emailDetails.setRecipient(optionalAdminDetails.get().getEmail());
        }
        email = optionalAdminDetails.get().getEmail();
        generatedCode.put(optionalAdminDetails.get().getEmail(), generateResetPassCode.generateCode());
        if(hasRights)
        {
            response = restTemplate.postForEntity(MAIL_URL + "/passcode", emailDetails, String.class).getBody();
        }
        return new BaseResponse<>("", HttpStatus.OK.value(), true, "", response);
    }

    @Override
    public BaseResponse<String> verifyCode(Integer id, String code, AdminDetails adminDetails) {
        Optional<AdminDetails> optionalAdminDetails = adminDetailsRepository.findById(id);
        if (optionalAdminDetails.isPresent()) {
            if (code != null) {
                if (code.equals(generatedCode.get(optionalAdminDetails.get().getEmail()))) {
                    if (optionalAdminDetails.get().isAuthority()) {
                        save(adminDetails);
                        return new BaseResponse<>("Code verified and Registered successful", HttpStatus.OK.value(), true, "", "Success");
                    } else {
                        return new BaseResponse<>("Not Authorized user", HttpStatus.FORBIDDEN.value(), false, "Cannot create account", "not authorized");
                    }
                }
            }
        }
        return new BaseResponse<>("Code not verified", HttpStatus.FORBIDDEN.value(), false, "", "Not Verified");
    }

    @Override
    public List<AdminDetails> getAllInfo() {
        return adminDetailsRepository.findAll();
    }

    @Override
    public Integer getId(String email) {
        Optional<Integer> optionalAdminId = adminDetailsRepository.fetchId(email);
        return optionalAdminId.orElse(null);
    }

    @Override
    public AdminDetails getInfoById(Integer id) {
        Optional<AdminDetails>  optionalAdminDetails = adminDetailsRepository.findById(id);
        if(optionalAdminDetails.isPresent())
        {
            optionalAdminDetails.get().setPassword("");
            return optionalAdminDetails.get();
        }
        return null;
    }

    @Override
    public String changePassword(UpdatePassword updatePassword) {
        generatedCode.remove(updatePassword.getEmail());
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        AdminDetails adminDetails = getInfoById(updatePassword.getId());
        if(Objects.nonNull(adminDetails)) {
            adminDetails.setPassword(bCryptPasswordEncoder.encode(updatePassword.getPassword()));
            adminDetailsRepository.save(adminDetails);
            return UPDATE_PASSWORD;
        }
        return null;
    }

    @Override
    public String forgotPasswordReset(UpdatePassword updatePassword) {
        BaseResponse<Map<String,String>> baseResponse = restTemplate.exchange(MAIL_URL + "/admin/fetch/forgot-password/code",HttpMethod.GET,null,new ParameterizedTypeReference<BaseResponse<Map<String,String>>>() {}).getBody();
        Map<String,String> forgotPassCode = baseResponse.getValue();
        if(updatePassword.getCode().equals(forgotPassCode.get(updatePassword.getEmail()))) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            Optional<AdminDetails> optionalAdminDetails = adminDetailsRepository.findByEmail(updatePassword.getEmail());
            if(optionalAdminDetails.isPresent())
            {
                optionalAdminDetails.get().setPassword(bCryptPasswordEncoder.encode(updatePassword.getPassword()));
                adminDetailsRepository.save(optionalAdminDetails.get());
                return UPDATE_PASSWORD;
            }

        }
        return null;
    }

    @Override
    public AdminDetails updateProfile(AdminDetails details, Integer id) {
        Optional<AdminDetails> optionalAdminDetails = adminDetailsRepository.findById(id);
        if(optionalAdminDetails.isPresent()) {
            details.setId(optionalAdminDetails.get().getId());
            BeanUtils.copyProperties(details, optionalAdminDetails.get());
            return adminDetailsRepository.save(optionalAdminDetails.get());
        }
        return null;
    }

    @Override
    public Boolean validateByEmail(String email) {
        Optional<AdminDetails> optionalAdminDetails = adminDetailsRepository.findByEmail(email);
        return optionalAdminDetails.isPresent();
    }

}
