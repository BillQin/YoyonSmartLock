package yoyon.smartlock.standalone.utils;

public class YoyonHash {
    private byte[] HASH_TA = {
            (byte)0x45, (byte)0x7B, (byte)0x65, (byte)0x3F, (byte)0xC9, (byte)0x09, (byte)0xFF, (byte)0x38, (byte)0x1F, (byte)0x87, (byte)0xED, (byte)0x67, (byte)0x03, (byte)0x73, (byte)0x95, (byte)0x67,
            (byte)0xFC, (byte)0xE3, (byte)0xF6, (byte)0x3C, (byte)0xBF, (byte)0xBE, (byte)0x72, (byte)0xAE, (byte)0x72, (byte)0x8E, (byte)0x0C, (byte)0xDD, (byte)0x0A, (byte)0xFF, (byte)0xEC, (byte)0xA6,
            (byte)0x48, (byte)0xE0, (byte)0x78, (byte)0xE0, (byte)0xAA, (byte)0xBF, (byte)0x3E, (byte)0x70, (byte)0xC7, (byte)0xDF, (byte)0x12, (byte)0x58, (byte)0xB0, (byte)0xCD, (byte)0x26, (byte)0x96,
            (byte)0x1B, (byte)0x27, (byte)0x7C, (byte)0x2B, (byte)0x57, (byte)0xDB, (byte)0x68, (byte)0x0B, (byte)0xF9, (byte)0x4A, (byte)0x7D, (byte)0xA9, (byte)0xBE, (byte)0x54, (byte)0xAB, (byte)0x5A,
            (byte)0xBB, (byte)0x47, (byte)0x73, (byte)0x5A, (byte)0xCA, (byte)0xDA, (byte)0x38, (byte)0x93, (byte)0x99, (byte)0x9B, (byte)0x99, (byte)0xAB, (byte)0xAD, (byte)0xDC, (byte)0x38, (byte)0x93,
            (byte)0xB6, (byte)0xD9, (byte)0x34, (byte)0x29, (byte)0x8D, (byte)0xD8, (byte)0x45, (byte)0xA2, (byte)0x59, (byte)0x9A, (byte)0x4A, (byte)0xD8, (byte)0x62, (byte)0x9B, (byte)0xB5, (byte)0xCD,
            (byte)0xDD, (byte)0x24, (byte)0x16, (byte)0x9F, (byte)0x5C, (byte)0x7D, (byte)0xD3, (byte)0x32, (byte)0x90, (byte)0x99, (byte)0x9B, (byte)0x3B, (byte)0x9B, (byte)0x20, (byte)0xF9, (byte)0x38,
            (byte)0x81, (byte)0x3C, (byte)0x1D, (byte)0xB5, (byte)0x35, (byte)0xA9, (byte)0xDF, (byte)0xAA, (byte)0x5E, (byte)0xDF, (byte)0x95, (byte)0xE8, (byte)0x4C, (byte)0x8D, (byte)0x94, (byte)0x6E,
            (byte)0xAD, (byte)0x9F, (byte)0xCA, (byte)0xBC, (byte)0x9A, (byte)0x78, (byte)0x67, (byte)0x75, (byte)0xF9, (byte)0xAA, (byte)0x59, (byte)0xFA, (byte)0x44, (byte)0xAD, (byte)0xDA, (byte)0x6B,
            (byte)0xCB, (byte)0x41, (byte)0x5F, (byte)0x96, (byte)0x3B, (byte)0xD5, (byte)0x37, (byte)0xE8, (byte)0x1B, (byte)0xF6, (byte)0xCF, (byte)0xFA, (byte)0x46, (byte)0x61, (byte)0x15, (byte)0x67,
            (byte)0x49, (byte)0x9D, (byte)0x5D, (byte)0xA1, (byte)0xDF, (byte)0xA4, (byte)0xA2, (byte)0x16, (byte)0xE5, (byte)0xBB, (byte)0xE7, (byte)0x57, (byte)0xD1, (byte)0xF6, (byte)0xF3, (byte)0xD7,
            (byte)0x01, (byte)0xA2, (byte)0xE5, (byte)0xFD, (byte)0x26, (byte)0x72, (byte)0x17, (byte)0x79, (byte)0xBA, (byte)0x3F, (byte)0xAA, (byte)0x78, (byte)0x61, (byte)0x35, (byte)0xA9, (byte)0x27,
            (byte)0xE3, (byte)0x84, (byte)0xB5, (byte)0x51, (byte)0x5B, (byte)0x2C, (byte)0x59, (byte)0xC5, (byte)0x0B, (byte)0xD6, (byte)0xC6, (byte)0x44, (byte)0xA7, (byte)0xD4, (byte)0x2F, (byte)0xF5,
            (byte)0xAE, (byte)0x20, (byte)0xA0, (byte)0x0D, (byte)0x94, (byte)0xB4, (byte)0xCA, (byte)0x03, (byte)0x86, (byte)0xB4, (byte)0xF9, (byte)0x80, (byte)0x18, (byte)0x58, (byte)0x1E, (byte)0xC8,
            (byte)0x97, (byte)0xD0, (byte)0xA5, (byte)0x1B, (byte)0x34, (byte)0x1F, (byte)0xF2, (byte)0xB5, (byte)0x6E, (byte)0xD8, (byte)0x93, (byte)0x36, (byte)0xCF, (byte)0x6C, (byte)0x2F, (byte)0xD1,
            (byte)0xE9, (byte)0x24, (byte)0x6A, (byte)0x9F, (byte)0x6B, (byte)0x66, (byte)0xB3, (byte)0xA9, (byte)0xF4, (byte)0x21, (byte)0x6D, (byte)0x71, (byte)0x66, (byte)0xAE, (byte)0xAE, (byte)0x2B
    };
    public static byte[] OTP_TA = {
            56, 51, 48, 52, 49, 50, 57, 53, 54, 52, 53, 48, 57, 49, 50, 56,
            54, 49, 55, 56, 48, 51, 49, 57, 53, 55, 49, 52, 50, 56, 48, 51,
            53, 51, 48, 55, 48, 51, 50, 57, 52, 51, 48, 53, 56, 55, 55, 56,
            54, 55, 52, 57, 49, 52, 56, 53, 48, 51, 54, 51, 52, 57, 55, 49,
            56, 52, 57, 54, 53, 50, 56, 54, 48, 49, 52, 55, 55, 52, 48, 50,
            49, 51, 48, 54, 50, 57, 50, 53, 57, 55, 52, 56, 49, 56, 52, 55,
            50, 48, 55, 49, 50, 51, 57, 54, 48, 56, 56, 50, 49, 56, 54, 48,
            53, 49, 57, 54, 51, 48, 53, 56, 55, 49, 55, 52, 54, 51, 57, 53,
            56, 48, 57, 53, 55, 51, 52, 53, 48, 54, 56, 51, 52, 56, 49, 55,
            50, 57, 56, 57, 54, 53, 52, 49, 57, 55, 49, 56, 51, 54, 49, 52,
            57, 53, 51, 56, 49, 54, 57, 51, 53, 48, 50, 54, 48, 57, 51, 50,
            49, 55, 52, 50, 48, 54, 56, 55, 55, 54, 57, 53, 51, 49, 48, 50,
            55, 51, 53, 49, 52, 50, 53, 51, 57, 52, 49, 54, 55, 50, 50, 57,
            53, 50, 52, 56, 48, 54, 49, 51, 57, 55, 54, 53, 56, 48, 51, 52,
            53, 48, 55, 50, 50, 57, 52, 53, 49, 48, 54, 53, 54, 51, 52, 50,
            53, 49, 52, 50, 55, 50, 57, 55, 52, 50, 56, 54, 57, 54, 53, 56
    };

    private byte[] YOYON_OTP_HASH(int input_Num,int output_len)
    {
        byte[] output_buf = new byte[output_len];
        long start_index,jump_index;
        int i;

        byte[] prim = {3,5,7,11,13,17,19};

        start_index = input_Num & 0x00000000FFFFFFFFL;

        start_index = start_index % HASH_TA.length;

        jump_index = input_Num & 0x00000000FFFFFFFFL;

        jump_index = jump_index % prim.length;


        for ( i = 0; i < output_len; i++)
        {
            output_buf[i] = HASH_TA[(int)(start_index + prim[(int)jump_index] * i) % HASH_TA.length];
        }

        return output_buf;
    }

    /**
     * @param input_buf 输入，密码的ASCII码字符串
     * @param input_len 输入，密码字符串的长度
     * @param output_len 输入，指定输出密码的ASCII码字符串长度
     * @param initial_key 输入，初始化密钥
     * @return 输出，密码的ASCII码字符串
     */
    public byte[] OTP_GENERATE(byte[] input_buf,int input_len,int output_len,int initial_key)
    {
        byte[] output_buf;
        int i;
        int otp_key,input_data;
        byte[] hash_key;

        otp_key = initial_key;
        for(i = 0;i < input_len; i++)
        {
            input_data = (input_buf[i] & 0xFF)|
                    ((input_buf[i] & 0xFF) << 8) |
                    ((input_buf[i] & 0xFF) <<16) |
                    ((input_buf[i] & 0xFF) << 24);

            otp_key ^= input_data;
            hash_key = YOYON_OTP_HASH(otp_key, 4);
            input_data = (hash_key[0] & 0xFF) |
                    ((hash_key[1] & 0xFF) << 8) |
                    ((hash_key[2] & 0xFF) <<16) |
                    ((hash_key[3] & 0xFF) << 24);

            otp_key ^= input_data;
        }

        output_buf = YOYON_OTP_HASH(otp_key, output_len);
        if(initial_key != 0)
        {
            for(i = 0;i < output_len; i++)
            {
                output_buf[i] = OTP_TA[(output_buf[i] & 0xFF)];
            }
        }

        return output_buf;
    }

    public int bytesToInt(byte[] bytes)
    {
        int result = 0;

//        for (byte aByte : bytes) {
//            result |= (aByte & 0xFF);
//        }
        if(bytes.length != 4)
            return 0;
        result = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        return result;
    }
}
            
            
