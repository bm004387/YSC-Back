-- 하단 탭용 프로그램과 메뉴 기본 데이터
-- 기존 pgm_mng / menu_mng 테이블이 ysc 스키마에 생성된 상태에서 실행합니다.
BEGIN;

INSERT INTO ysc.pgm_mng
    (pgm_cd, pgm_nm, pgm_src_pth, pgm_url, pgm_desc, use_yn,
     frst_reg_user_id, frst_reg_dtm, last_mod_user_id, last_mod_dtm)
VALUES
    ('HOME00001', '홈', 'src/screens/MainScreen.tsx', 'main', '홈 화면', 'Y', 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'), 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS')),
    ('MYINFO001', '내 정보', 'src/screens/MyInfoScreen.tsx', 'myInfo', '내 정보 화면', 'Y', 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'), 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS')),
    ('SETTING01', '설정', 'src/screens/SettingsScreen.tsx', 'settings', '설정 화면', 'Y', 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'), 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'))
ON CONFLICT (pgm_cd) DO UPDATE SET
    pgm_nm = EXCLUDED.pgm_nm,
    pgm_src_pth = EXCLUDED.pgm_src_pth,
    pgm_url = EXCLUDED.pgm_url,
    pgm_desc = EXCLUDED.pgm_desc,
    use_yn = EXCLUDED.use_yn,
    last_mod_user_id = 'SYSTEM',
    last_mod_dtm = TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS');

INSERT INTO ysc.menu_mng
    (menu_id, menu_nm, up_menu_id, menu_lvl, sort_ord, menu_typ, pgm_cd,
     ico_nm, menu_desc, use_yn, frst_reg_user_id, frst_reg_dtm,
     last_mod_user_id, last_mod_dtm)
VALUES
    ('10010001', '홈', NULL, 1, 1, 'TAB', 'HOME00001', 'home', '홈 탭', 'Y', 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'), 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS')),
    ('10010002', '내 정보', NULL, 1, 2, 'TAB', 'MYINFO001', 'user', '내 정보 탭', 'Y', 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'), 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS')),
    ('10010003', '설정', NULL, 1, 3, 'TAB', 'SETTING01', 'settings', '설정 탭', 'Y', 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'), 'SYSTEM', TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'))
ON CONFLICT (menu_id) DO UPDATE SET
    menu_nm = EXCLUDED.menu_nm,
    up_menu_id = EXCLUDED.up_menu_id,
    menu_lvl = EXCLUDED.menu_lvl,
    sort_ord = EXCLUDED.sort_ord,
    menu_typ = EXCLUDED.menu_typ,
    pgm_cd = EXCLUDED.pgm_cd,
    ico_nm = EXCLUDED.ico_nm,
    menu_desc = EXCLUDED.menu_desc,
    use_yn = EXCLUDED.use_yn,
    last_mod_user_id = 'SYSTEM',
    last_mod_dtm = TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS');

COMMIT;
