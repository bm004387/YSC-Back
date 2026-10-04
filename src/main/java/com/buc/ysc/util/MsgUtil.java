package com.buc.ysc.util;

import com.buc.ysc.common.msg.mapper.MsgMapper;
import com.buc.ysc.common.msg.vo.MsgVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MsgUtil {

    private static final String MSG_KEY_PREFIX = "MSG:";
    private final MsgMapper msgMapper;


    private final RedisTemplate<String, String> msgRedisTemplate;


    public String getMsg(String menuId, String msgCd) {

        Map<String, String> msgMap = getMsgMap(menuId);
        String msg = msgMap.get(msgCd);

        if (msg == null || msg.isBlank()) {
            return menuId + "_" + msgCd;
        }

        return msg;
    }

    public Map<String, String> getMsgMap(String menuId) {

        String key = MSG_KEY_PREFIX + menuId;

        // 1. Redis에서 메뉴 전체 메시지 조회
        Map<Object, Object> redisMap = msgRedisTemplate.opsForHash().entries(key);
        if (redisMap != null && !redisMap.isEmpty()) {
            Map<String, String> msgMap = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
                msgMap.put(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
            }
            return msgMap;
        }

        // 2. Redis에 없으면 DB 조회
        List<MsgVO> msgList = msgMapper.getMsgList(menuId);

        Map<String, String> msgMap = new LinkedHashMap<>();

        for (MsgVO msg : msgList) {
            msgMap.put(msg.getMsgCd(), msg.getMsgCn());
        }

        // 3. Redis에 메뉴 전체 메시지 저장
        if (!msgMap.isEmpty()) {
            Map<String, String> redisData = new LinkedHashMap<>(msgMap);

            msgRedisTemplate.opsForHash().putAll(key, redisData);
        }

        return msgMap;
    }
}