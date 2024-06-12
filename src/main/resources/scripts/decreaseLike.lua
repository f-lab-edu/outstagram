---@diagnostic disable: undefined-global
local likeCountKey = KEYS[1]
local userLikeKey = KEYS[2]
local postId = tonumber(ARGV[1])

-- 사용자 좋아요 기록을 확인
local userLikes = redis.call('lrange', userLikeKey, 0, -1)

if not userLikes or #userLikes == 0 then
    return redis.error_reply('LIKE NOT FOUND')
end

local parsedValue = {}
local found = false

for i, v in ipairs(userLikes) do
    local decodedValue = cjson.decode(v)
    if decodedValue.postId == postId then
        found = true
    else
        table.insert(parsedValue, v)
    end
end

if found then
    -- 해당 키의 값들 지우고
    redis.call('del', userLikeKey)
    -- 삭제된 거 빼고 다시 값 push하기
    for _, v in ipairs(parsedValue) do
        redis.call('rpush', userLikeKey, v)
    end
    -- 좋아요 개수 1 감소
    redis.call('decr', likeCountKey)
    return postId .. " removed successfully"
else
    return redis.error_reply('LIKE NOT FOUND')
end