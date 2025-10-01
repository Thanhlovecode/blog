package com.example.blog.service.implement;

import com.example.blog.domain.Comment;
import com.example.blog.domain.Post;
import com.example.blog.domain.User;
import com.example.blog.dto.request.CommentRequest;
import com.example.blog.dto.request.CommentUpdateRequest;
import com.example.blog.dto.response.CommentResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.CommentService;
import com.example.blog.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j(topic = "COMMENT-SERVICE")
@RequiredArgsConstructor

public class CommentServiceImpl implements CommentService {

    private static final String MESSAGE_COMMENT_DELETED = " This comment has been deleted";

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentResponse> getTop5CommentByPostId(Long postId) {

        List<Comment> comments = commentRepository.findTop5CommentsByPostIdAndParentCommentIdIsNull(postId);
        if (comments.isEmpty()) {
            return null;
        }

        List<Long> commentIds = comments
                .stream()
                .map(Comment::getId)
                .toList();

        List<Comment> replies = commentRepository.findCommentsByParentCommentIdIn(commentIds);

        Map<Long, List<Comment>> repliesMap = replies
                .stream()
                .collect(Collectors.groupingBy(Comment::getParentCommentId));

        return comments
                .stream()
                .map(rootComment -> {
                    CommentResponse commentResponse = commentMapper.toCommentResponse(rootComment);
                    List<Comment> repliesLComment = repliesMap.getOrDefault(rootComment.getId(), Collections.emptyList());
                    List<CommentResponse> repliesCommentResponse = repliesLComment
                            .stream()
                            .map(commentMapper::toCommentResponse)
                            .toList();
                    commentResponse.setReplies(repliesCommentResponse);
                    return commentResponse;
                })
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse saveComment(CommentRequest commentRequest) {

        User user = getCurrentUserByUsername();

        Post post = postRepository.getReferenceById(commentRequest.postId());

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(commentRequest.content())
                .parentCommentId(commentRequest.parentCommentId())
                .displayName(user.getFullName())
                .userAvatar(user.getProfile().getThumbnailUrl())
                .username(user.getUsername())
                .build();

        commentRepository.save(comment);
        log.info("create comment for post id by username successfully {}, {}", post.getId(), user.getUsername());

        return commentMapper.toCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse deleteComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        comment.setIsDeleted(true);
        comment.setContent(MESSAGE_COMMENT_DELETED);
        commentRepository.save(comment);
        return commentMapper.toCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest commentRequest) {

        Comment comment = getCommentById(commentId);

        comment.setContent(commentRequest.content());

        commentRepository.save(comment);

        return commentMapper.toCommentResponse(comment);
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private User getCurrentUserByUsername() {
        String username = SecurityUtils.getCurrentUserName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}

