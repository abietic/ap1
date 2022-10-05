-- 在lua脚本中，有两个全局的变量，是用来接收redis应用端传递的键值和其它参数的，
-- 分别为KEYS、ARGV。

-- 在应用端传递给KEYS时是一个数组列表，在lua脚本中通过索引方式获取数组内的值。

-- 在应用端，传递给ARGV的参数比较灵活，可以是多个独立的参数，但对应到Lua脚本中是，
-- 统一用ARGV这个数组接收，获取方式也是通过数组下标获取。
-- tonumber 把字符串转成数值
local seq_val_key = KEYS[1]
local seq_step_key = KEYS[2]

if tonumber(redis.call('EXISTS', seq_val_key)) ~= 0 and tonumber(redis.call('EXISTS', seq_step_key)) ~= 0 then
    local seq = tonumber(redis.call('GET', seq_val_key))
    local step = tonumber(redis.call('GET', seq_step_key))
    redis.call('INCRBY', seq_val_key, step)
    return seq
else
    return -1
end


