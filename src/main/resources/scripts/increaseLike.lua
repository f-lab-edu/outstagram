---@diagnostic disable: undefined-global
local likeCountKey = KEYS[1]
local userLikeKey = KEYS[2]
local likeRecord = ARGV[1]

-- 사용자 좋아요 기록을 확인
local userLikes = redis.call('lrange', userLikeKey, 0, -1)
for i, v in ipairs(userLikes) do
    if v == likeRecord then
        return redis.error_reply('DUPLICATED_LIKE')
    end
end

-- 사용자 좋아요 기록 추가 및 좋아요 수 증가를 원자적으로 처리
redis.call('lpush', userLikeKey, likeRecord)
redis.call('expire', userLikeKey, 3600) -- TTL 1시간 설정
redis.call('incr', likeCountKey) -- 좋아요 개수 증가

return 'OK'
