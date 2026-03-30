<?php
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// Load Composer's autoloader or manually include files if not using Composer
// Assuming user might place PHPMailer in a specific folder or use Composer
// For this environment, we'll try to include relative paths if they exist, 
// otherwise we fail silently or log error to avoid breaking the app.

require 'mail_config.php';

function sendEmail($to, $subject, $body) {
    
    // Check if PHPMailer exists (User needs to install it)
    if (!class_exists('PHPMailer\PHPMailer\PHPMailer')) {
        // Fallback or just return false
        // Try to include if manual install
        if (file_exists('PHPMailer/src/PHPMailer.php')) {
            require 'PHPMailer/src/Exception.php';
            require 'PHPMailer/src/PHPMailer.php';
            require 'PHPMailer/src/SMTP.php';
        } else {
            return false; // PHPMailer not found
        }
    }

    $mail = new PHPMailer(true);

    try {
        //Server settings
        $mail->isSMTP();
        $mail->Host       = SMTP_HOST;
        $mail->SMTPAuth   = true;
        $mail->Username   = SMTP_USER;
        $mail->Password   = SMTP_PASS;
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
        $mail->Port       = SMTP_PORT;

        //Recipients
        $mail->setFrom(SMTP_USER, 'E-Vote Admin');
        $mail->addAddress($to);

        //Content
        $mail->isHTML(true);
        $mail->Subject = $subject;
        $mail->Body    = $body;

        $mail->send();
        return true;
    } catch (\Throwable $e) {
        return false;
    }
}
?>
