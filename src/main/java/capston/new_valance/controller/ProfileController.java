package capston.new_valance.controller;

import capston.new_valance.dto.req.ProfilePatchRequest;
import capston.new_valance.dto.res.DailyWatchCountResponse;
import capston.new_valance.dto.res.ProfileResponse;
import capston.new_valance.jwt.UserPrincipal;
import capston.new_valance.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Slf4j
public class ProfileController {

    private final ProfileService profileService;


    // 1. 프로필 조회 GET /api/profile
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        ProfileResponse profile = profileService.getProfile(principal.getUserId());
        return ResponseEntity.ok(profile);
    }

    // 2. 프로필 수정 PATCH /api/profile
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute ProfilePatchRequest req
    ) {

        ProfileResponse updated = profileService.updateProfile(principal.getUserId(), req);

        return ResponseEntity.ok(updated);
    }

    // 3. 주간 조회 GET /api/profile/week
    @GetMapping("/week")
    public ResponseEntity<List<List<DailyWatchCountResponse>>> getWeeklyWatchCounts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<List<DailyWatchCountResponse>> result = profileService.getWeeklyWatchCounts(principal.getUserId());
        return ResponseEntity.ok(result);
    }
}
