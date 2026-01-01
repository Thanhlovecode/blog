package com.example.blog.service.implement;

import com.example.blog.domain.Follow;
import com.example.blog.domain.Post;
import com.example.blog.domain.Profile;
import com.example.blog.domain.User;
import com.example.blog.dto.response.FollowResponse;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.ProfileMapper;
import com.example.blog.repository.FollowRepository;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.FollowService;
import com.example.blog.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "FOLLOW-SERVICE")
public class FollowServiceImpl implements FollowService {


    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;


    @Override
    public PageResponse<FollowResponse> getFollowingByUserId(Long userId,int page) {
        Pageable pageable = PageUtils.defaultNoSortPageable(page);
        return getFollowPageResponse(
                ()-> followRepository.findFollowingByUserId(userId,pageable)
        );
    }

    @Override
    public PageResponse<FollowResponse> getFollowerByUserId(Long userId,int page) {
        Pageable pageable = PageUtils.defaultNoSortPageable(page);
        return getFollowPageResponse(
                ()-> followRepository.findFollowerByUserId(userId,pageable)
        );
    }


    private PageResponse<FollowResponse> getFollowPageResponse(Supplier<Page<Long>> supplier) {
        Page<Long> pageListUserId = supplier.get();

        if(pageListUserId.getContent().isEmpty()) {
            return null;
        }

        List<Profile> profiles = profileRepository.findProfileByIds(pageListUserId.getContent());

        return PageResponse.fromPage(pageListUserId, convertToListFollowResponse(profiles));
    }

    private List<FollowResponse> convertToListFollowResponse(List<Profile> profiles) {
        return profiles.stream()
                .map(profileMapper::toFollowResponse)
                .toList();
    }

    @Override
    @Transactional
    public void followUser(Long currentUserId, Long targetUserId) {
        validateNotSelfFollow(currentUserId, targetUserId);

        User currentUser  = userRepository.getReferenceById(currentUserId);
        User targetUser  = userRepository.getReferenceById(targetUserId);

        Follow follow = Follow.builder()
                .follower(currentUser)
                .following(targetUser)
                .build();

        followRepository.save(follow);

        updateCountersAfterFollow(currentUserId, targetUserId);

        log.info("User [{}] followed user [{}]", currentUserId, targetUserId);

    }

    @Override
    @Transactional
    public void unfollowUser(Long currentUserId, Long userIdToUnfollow) {
        validateNotSelfFollow(currentUserId, userIdToUnfollow);

        Follow follow = followRepository.findByFollowerIdAndFollowingId(currentUserId,userIdToUnfollow)
                .orElseThrow(()-> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        followRepository.delete(follow);

        updateCountersAfterUnfollow(currentUserId, userIdToUnfollow);

        log.info("User [{}] unfollowed user [{}]", currentUserId, userIdToUnfollow);
    }

    private void updateCountersAfterFollow(Long followerId, Long followingId) {
        userRepository.incrementFollowingCount(followerId);
        userRepository.incrementFollowerCount(followingId);
    }

    private void updateCountersAfterUnfollow(Long followerId, Long followingId) {
        userRepository.decrementFollowingCount(followerId);
        userRepository.decrementFollowerCount(followingId);
    }

    private void validateNotSelfFollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.FOLLOW_INVALID);
        }
    }
}
