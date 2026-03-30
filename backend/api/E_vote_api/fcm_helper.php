<?php

/**
 * FCM Helper to send push notifications via Firebase HTTP v1 API.
 * Uses a manually constructed JWT for authentication.
 */
class FcmHelper {
    private $key_file = 'firebase_key.json';
    private $cached_token = null;

    public function sendNotification($token, $title, $body, $data = []) {
        $access_token = $this->getAccessToken();
        if (!$access_token) return false;

        $project_id = json_decode(file_get_contents($this->key_file), true)['project_id'];
        $url = "https://fcm.googleapis.com/v1/projects/$project_id/messages:send";

        $message = [
            'message' => [
                'token' => $token,
                'notification' => [
                    'title' => $title,
                    'body' => $body
                ],
                'data' => (object)$data
            ]
        ];

        $headers = [
            'Authorization: Bearer ' . $access_token,
            'Content-Type: application/json'
        ];

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));
        
        $response = curl_exec($ch);
        $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        return $http_code === 200;
    }

    private function getAccessToken() {
        if ($this->cached_token) return $this->cached_token;
        if (!file_exists($this->key_file)) return null;
        $key_data = json_decode(file_get_contents($this->key_file), true);

        $header = json_encode(['alg' => 'RS256', 'typ' => 'JWT']);
        $now = time();
        $payload = json_encode([
            'iss' => $key_data['client_email'],
            'scope' => 'https://www.googleapis.com/auth/cloud-platform',
            'aud' => 'https://oauth2.googleapis.com/token',
            'exp' => $now + 3600,
            'iat' => $now
        ]);

        $base64UrlHeader = $this->base64UrlEncode($header);
        $base64UrlPayload = $this->base64UrlEncode($payload);

        $signature = '';
        openssl_sign($base64UrlHeader . "." . $base64UrlPayload, $signature, $key_data['private_key'], 'SHA256');
        $base64UrlSignature = $this->base64UrlEncode($signature);

        $jwt = $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://oauth2.googleapis.com/token');
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt
        ]));

        $response = curl_exec($ch);
        $data = json_decode($response, true);
        curl_close($ch);

        $this->cached_token = $data['access_token'] ?? null;
        return $this->cached_token;
    }

    private function base64UrlEncode($data) {
        return str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($data));
    }
}
?>
