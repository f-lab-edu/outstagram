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
    -- Clear the original list
    redis.call('del', userLikeKey)
    -- Insert the remaining values back to the list
    for _, v in ipairs(parsedValue) do
        redis.call('rpush', userLikeKey, v)
    end
    redis.call('decr', likeCountKey)
    return postId .. " removed successfully"
else
    return redis.error_reply('LIKE NOT FOUND')
end
