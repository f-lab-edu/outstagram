package com.outstagram.outstagram.util;

import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import lombok.Data;

@Data
public class Snowflake {

    private static final int UNUSED_BITS = 1;       // 부호 비트(항상 0)
    private static final int EPOCH_BITS = 41;       // timestamp(현재 timestamp - 기준 timestamp)
    private static final int NODE_ID_BITS = 10;     // 장비 고유 번호(주로 DB 고유 번호)
    private static final int SEQUENCE_BITS = 12;    // 동일 밀리초에서 동시에 ID 생성할 수 있도록 기계별 시퀀스 번호

    private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;     // 장비 고유 번호 최댓값은 2^10 - 1
    private static final long maxSequence =
        (1L << SEQUENCE_BITS) - 1;  // 시퀀스 번호 최댓값은 2^12 - 1 (동일 밀리초에서 동시에 최대 4095개 id 생성 가능)

    // 기준 타임스탬프 : 2010년 11월 4일 10시 42분 54초
    private static final long DEFAULT_CUSTOM_EPOCH = 1288834974657L;    // BASE_TIMESTAMP로 변수명 수정하기

    private final long nodeId;
    private final long customEpoch;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public Snowflake(long nodeId, long customEpoch) {
        if (nodeId < 0 || nodeId > maxNodeId) {
            throw new ApiException(ErrorCode.NODEID_INVALID_RANGE);
        }
        this.nodeId = nodeId;
        this.customEpoch = customEpoch;
    }

    public Snowflake(long nodeId) {
        this(nodeId, DEFAULT_CUSTOM_EPOCH);
    }

    public Snowflake() {
        this.nodeId = createNodeId();
        this.customEpoch = DEFAULT_CUSTOM_EPOCH;
    }

    public synchronized long nextId() {
        // timestamp 값 구하기
        long curTimestamp = timestamp();

        if (curTimestamp < lastTimestamp) {
            throw new ApiException(ErrorCode.TIMESTAMP_INVALID);
        }

        // 동시 요청이 여러 개인 경우, sequence 값 세팅
        if (curTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // 같은 밀리초에 4095개 요청이 처리되서 sequence가 소진된 상황 -> 다음 밀리초될 때까지 대기
                curTimestamp = waitNextMills(curTimestamp);
            }
        } else {
            // 직전 timestamp와 다르다는 건 다른 밀리초라는 것 -> sequence 값을 0으로 초기화
            sequence = 0;
        }

        lastTimestamp = curTimestamp;

        // ID 생성
        return curTimestamp << (NODE_ID_BITS + SEQUENCE_BITS)
            | nodeId << NODE_ID_BITS
            | sequence;
    }

    private long timestamp() {
        return Instant.now().toEpochMilli() - customEpoch;
    }

    private long waitNextMills(long curTimestamp) {
        while (curTimestamp == lastTimestamp) {
            curTimestamp = timestamp();
        }

        return curTimestamp;
    }


    private long createNodeId() {
        long nodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            // 각 네트워크 인터페이스에서 mac 주소 가져오기
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for(byte macPort: mac) {
                        // MAC 주소의 각 바이트를 2자리 16진수 문자열로 포맷팅하기
                        sb.append(String.format("%02X", macPort));
                    }
                }
            }
            // MAC 주소들의 조합을 해시코드로 변환해 nodeId로 사용
            nodeId = sb.toString().hashCode();
        } catch (Exception ex) {
            // 네트워크 인터페이스 가져오다가 예외 발생하면 랜덤 값을 사용해 nodeId 생성
            nodeId = (new SecureRandom().nextInt());
        }

        // nodeId 범위 제한
        nodeId = nodeId & maxNodeId;
        return nodeId;
    }

}
