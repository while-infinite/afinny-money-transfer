package by.afinny.moneytransfer.openfeign.user;

import by.afinny.moneytransfer.dto.ResponseClientDataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient("USER-SERVICE/auth/information")
public interface UserInformationClient {

    @GetMapping
    ResponseEntity<ResponseClientDataDto> getClientData(@RequestParam UUID clientId);
}
