/*
 * Copyright (c) 2021 Windscribe Limited.
 */
package com.windscribe.vpn.constants

object PreferencesKeyConstants {
    const val SESSION_HASH = "session_auth_hash"
    const val CONNECTION_STATUS = "connection_status"
    const val USER_STATUS = "is_premium_user"
    const val USER_ACCOUNT_STATUS = "status"
    const val USER_SESSION_RESPONSE = "user_session_data"
    const val USER_REGISTRATION_RESPONSE = "user_reg_data"
    const val GET_SESSION = "get_session_data"
    const val PORT_MAP = "port_map_data"
    const val OPEN_VPN_SERVER_CONFIG = "server_config"
    const val OPEN_VPN_CREDENTIALS = "server_credentials"
    const val IKEV2_CREDENTIALS = "IKev2_server_credentials"
    const val NEWS_FEED_RESPONSE = "news_feed_response"
    const val NEWS_FEED_ALERT = "news_feed_alert"
    const val NEW_INSTALLATION = "new_installation"
    const val I_NEW = "new"
    const val I_OLD = "old"
    const val ON_CREATE_APPLICATION = "on_create_application"
    const val CHOSEN_PROTOCOL = "chosen_protocol"
    const val CONNECTION_MODE_KEY = "connection_mode"
    const val CONNECTION_MODE_AUTO = "Auto"
    const val CONNECTION_MODE_MANUAL = "Manual"
    const val PROTOCOL_KEY = "protocol"
    const val PROTO_UDP = "udp"
    const val PROTO_TCP = "tcp"
    const val PROTO_STEALTH = "stunnel"
    const val PROTO_IKev2 = "ikev2"
    const val PROTO_WIRE_GUARD = "wg"
    const val SAVED_UDP_PORT = "saved_udp_port"
    const val SAVED_TCP_PORT = "saved_tcp_port"
    const val SAVED_STEALTH_PORT = "saved_stealth_port"
    const val SAVED_IKev2_PORT = "saved_IkEv2_port"
    const val SAVED_WIRE_GUARD_PORT = "saved_wire_guard_port"
    const val DEFAULT_STEALTH_LEGACY_PORT = "443"
    const val DEFAULT_UDP_LEGACY_PORT = "443"
    const val DEFAULT_TCP_LEGACY_PORT = "443"
    const val DEFAULT_WIRE_GUARD_PORT = "443"
    const val DEFAULT_IKEV2_PORT = "500"
    const val USER_LANGUAGE = "locale"
    const val DEFAULT_LANGUAGE = "English (en)"
    const val WHITELIST_OVERRIDE = "whitelist_override"
    const val ALWAYS_ON = "always_on"
    const val GLOBAL_CONNECTION_PREFERENCE = "global_preference"
    const val USER_IP = "user_ip"
    const val USER_NAME = "user_name"
    const val BEST_LOCATION_IP_2 = "best_location_ip_2"
    const val DATA_LEFT = "data_left"
    const val DATA_USED = "data_used"
    const val DATA_MAX = "data_max"
    const val DEBUG_LOG_FILE_NAME = "/applog.txt"
    const val LOCATION_REVISION = "loc_rev"
    const val LOCATION_HASH = "loc_hash"
    const val ALC_LIST = "alc_list"
    const val EMAIL_ADDRESS = "email_address"
    const val VPN_CONNECTED = "connected"
    const val VPN_CONNECTING = "connecting"
    const val VPN_WAITING_FOR_SERVER_REPLY = "waiting_for_server"
    const val RECONNECTING_AFTER_NETWORK = "reconnecting_after_network"
    const val SELECTED_LOCATION_UPDATE = "selected_location_update"
    const val PROTOCOL_SWITCH = "com/windscribe/vpn/protocol_switch"
    const val PROTOCOL_SWITCH_UPDATE = "protocol_switch_update"
    const val PROTOCOL_SWITCH_EXTRA = "protocol_extra"
    const val VPN_DISCONNECTING = "disconnecting"
    const val VPN_CONNECTIVITY_TEST = "connectivity_test"
    const val VPN_CONNECTIVITY_TEST_FAILED = "connectivity_test_failed"
    const val VPN_IP_RECEIVED = "connectivity_ip_received"
    const val VPN_IP_EXTRA = "vpn_ip_extra"
    const val VPN_CONNECTIVITY_RETRY = "connectivity_retry"
    const val VPN_DISCONNECTED = "disconnected"
    const val VPN_NO_NETWORK = "nonetwork"
    const val INVALID_SESSION = "invalid_session"
    const val VPN_REQUIRES_USER_INPUT = "vpn_requires_user_vpn"
    const val VPN_AUTHENTICATION_FAILURE = "vpn_authentication_failure"
    const val TUNNEL_ERROR = "tunnel_error"
    const val TUNNEL_ERROR_EXTRA = "tunnel_error_extra"
    const val UNKNOWN_ERROR = -1
    const val ADDRESS_ALREADY_USED = 1
    const val ACCESS_API_IP_1 = "access_api_ip_1"
    const val ACCESS_API_IP_2 = "access_api_ip_2"
    const val STATIC_IP_COUNT = "static_ip_count"
    const val IS_CONNECTING_TO_STATIC_IP = "is_connecting_static"
    const val IS_CONNECTING_TO_CONFIGURED_IP = "is_connecting_configured"
    const val STATIC_IP_CREDENTIAL = "static_ip_credentials"
    const val PREVIOUS_USER_STATUS = "previous_user_status"
    const val PREVIOUS_ACCOUNT_STATUS = "previous_account_status"
    const val AUTH_RECONNECT_ATTEMPT_COUNT_KEY = "connection_attempt"
    const val SHOW_LATENCY_IN_MS = "show_latency_in_ms"
    const val CONNECTION_ATTEMPT = "current_tag"
    const val CONNECTION_RETRY_ENABLED = "connection_retry_enabled"
    const val VPN_CONNECTIVITY_RETRY_METHOD = "connectivity_retry_method"
    const val PURCHASE_FLOW_STATE_KEY = "purchase_flow_state"
    const val SELECTION_KEY = "list_selection_key"

    // if values changes update sort by string array
    const val DEFAULT_LIST_SELECTION_MODE = "Geography"
    const val AZ_LIST_SELECTION_MODE = "Alphabet"
    const val LATENCY_LIST_SELECTION_MODE = "Latency"
    const val AUTO_MTU_MODE_KEY = "auto_mtu_mode"
    const val LAST_MTU_VALUE = "last_mtu_value"
    const val DARK_THEME = "Dark"
    const val SELECTED_THEME = "selected_theme"
    const val EXCLUSIVE_MODE = "Exclusive"
    const val INCLUSIVE_MODE = "Inclusive"
    const val SPLIT_ROUTING_MODE = "split_routing_mode"
    const val SPLIT_TUNNEL_TOGGLE = "tunnel_toggle"
    const val AUTO_START_ON_BOOT = "auto_start_boot"
    const val INSTALLED_APPS_DATA = "installed_app_data"
    const val LAST_CONNECTION_USING_SPLIT = "last_connection_using_split"
    const val RECONNECT_REQUIRED = "reconnect_required"
    const val NOTIFICATION_STAT = "notification_stat"
    const val LAN_BY_PASS = "lan_by_pass"
    const val USER_ACCOUNT_UPDATE_REQUIRED = "user_account_update_required"
    const val PING_UPDATE_REQUIRED = "ping_update_required"
    const val GPS_SPOOF_SETTING = "gps_spoof_setting"
    const val SELECTED_CITY_ID = "selected_city_id"
    const val LOWEST_PING_ID = "lowest_ping_id"
    const val PROTOCOL_SWITCH_FINISH = "protocol_switch_finish"
    const val NO_MORE_PROTOCOLS = "no_more_protocols"
    const val LOGIN_TIME = "login_time"
    const val OUR_IP = "our_ip"
    const val KEEP_ALIVE = "keep_alive"
    const val KEEP_ALIVE_MODE_AUTO = "keep_alive_mode_auto"
    const val BLUR_IP = "blur_ip"
    const val BLUR_NETWORK_NAME = "blur_network_name"
    const val LAST_SELECTED_SERVER_TAB = "last_selected_server_tab"
    const val WIRE_GUARD_CONFIG = "wire_guard_config"
    const val PORT_MAP_VERSION = "port_map_version"
    const val HAPTIC_FEEDBACK = "haptic_feedback"
    const val EMAIL_STATUS = "email_status"
    const val CUSTOM_FLAG_BACKGROUND = "custom_flag_background"
    const val DISCONNECTED_FLAG_PATH = "disconnected_flag_path"
    const val CONNECTED_FLAG_PATH = "connected_flag_path"
    const val FLAG_VIEW_HEIGHT = "flag_view_height"
    const val FLAG_VIEW_WIDTH = "flag_view_width"
    const val DISABLE_KERNEL_MODULE = "disable_kernel_module"
    const val MULTIPLE_TUNNELS = "multiple_tunnels"
    const val STARTED_BY_ALWAYS_ON = "started_by_always_on"
    const val ACTION_ADD_EMAIL_FROM_ACCOUNT = "add_email_from_account"
    const val ACTION_RESEND_EMAIL_FROM_ACCOUNT = "resend_email_from_account"
    const val ACTION_ADD_EMAIL_FROM_LOGIN = "add_email_from_login"
    const val LAN_ALLOW = "lan_allow"
    const val LAN_BLOCK = "lan_block"
    const val DISABLED_MODE = "disabled_mode"
    const val BOOT_ALLOW = "boot_on_start_allow"
    const val BOOT_BLOCK = "boot_on_start_block"
    const val FAVORITE_SERVER_LIST = "favorite_server_list"
    const val FUTURE_SELECTED_CITY = "future_selected_city"
    const val USER_INTENDED_DISCONNECT = "user_intended_disconnect"
    const val ROBERT_SETTINGS = "robert_settings"
    const val SHOW_LOCATION_HEALTH = "show_location_health"
    const val WG_LOCAL_PARAMS = "wg_local_params"
    const val DECOY_TRAFFIC = "decoy_traffic"
    const val FAKE_TRAFFIC_VOLUME = "fake_traffic_volume"
}
