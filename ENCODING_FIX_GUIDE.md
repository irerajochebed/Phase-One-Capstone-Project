# Character Encoding Fix Guide

## Problem
Your Java source files contain garbled characters (mojibake) like:
- `â€"` instead of `-` (dash)
- `â€¢` instead of `•` (bullet)
- `ðŸ"'` instead of emoji icons
- `âœ"` instead of checkmarks

## Root Cause
UTF-8 encoded characters are being interpreted as Windows-1252, causing display issues.

## Solution Options

### Option 1: Use Plain ASCII (Recommended for Compatibility)

Replace all special characters with plain ASCII equivalents:

**In IgirePayApp.java:**
```java
// Change this:
stage.setTitle("IgirePay â€" Digital Wallet");

// To this:
stage.setTitle("IgirePay - Digital Wallet");
```

**In Console menus:**
```java
// Change this:
default -> System.out.println("Invalid option. Please enter 0â€"4.");

// To this:
default -> System.out.println("Invalid option. Please enter 0-4.");
```

**For emojis in NotificationsContentController.java:**
```java
// Change emoji icons to text labels:
case Notification.TYPE_SENT -> return "[SENT]";
case Notification.TYPE_RECEIVED -> return "[RECEIVED]";
case Notification.TYPE_DEPOSIT -> return "[DEPOSIT]";
// etc.
```

### Option 2: Fix UTF-8 Encoding in IDE

1. **IntelliJ IDEA:**
   - File → Settings → Editor → File Encodings
   - Set "Global Encoding" to UTF-8
   - Set "Project Encoding" to UTF-8
   - Check "Transparent native-to-ascii conversion"

2. **VS Code:**
   - Click encoding in status bar (bottom right)
   - Select "Reopen with Encoding"
   - Choose "UTF-8"
   - Save file

3. **Eclipse:**
   - Right-click project → Properties
   - Resource → Text file encoding
   - Select "UTF-8"

### Option 3: Automated Fix with Find & Replace

Use your IDE's Find & Replace (Ctrl+Shift+H) across all files:

| Find | Replace With |
|------|--------------|
| `â€"` | `-` |
| `â€¢` | `•` or `-` |
| `âœ"` | `✓` or `[OK]` |
| `âœ—` | `✗` or `[X]` |
| `ðŸ"'` | `[LOCKED]` |
| `ðŸ"­` | `` (empty) |
| `ðŸ"¤` | `[SENT]` |
| `ðŸ"¥` | `[RECEIVED]` |
| `ðŸ'°` | `[MONEY]` |
| `ðŸ''` | `[ADMIN]` |
| `ðŸ""` | `[BELL]` |
| `ðŸ ` | `[HOME]` |
| `âœˆ` | `[SEND]` |
| `ðŸ"Š` | `[CHART]` |
| `âš™` | `[SETTINGS]` |
| `ðŸ—'` | `[DELETE]` |

## Files That Need Fixing

Based on the grep search, these files contain encoding issues:

### High Priority (UI visible):
1. `ui/IgirePayApp.java` - Window title
2. `ui/controller/NotificationsContentController.java` - Notification icons
3. `ui/controller/MainShellController.java` - Navigation icons
4. `ui/controller/RegisterController.java` - Validation messages
5. `ui/controller/SendMoneyController.java` - Transfer UI
6. `ui/controller/WithdrawController.java` - Withdrawal UI

### Medium Priority (Console UI):
7. `LAB3/ui/ConsoleApp.java` - Console header
8. `LAB3/ui/AdminMenu.java` - Admin menu
9. `LAB3/ui/CustomerMenu.java` - Customer menu
10. `LAB3/ui/TransactionMenu.java` - Transaction menu
11. `LAB3/ui/AccountMenu.java` - Account menu

## Quick Fix for Critical Files

### Fix Window Title
**File:** `src/main/java/com/igirepay/igirepaypaymentgateway/ui/IgirePayApp.java`

Find line 22 and change to:
```java
stage.setTitle("IgirePay - Digital Wallet");
```

### Fix Navigation Icons
**File:** `src/main/java/com/igirepay/igirepaypaymentgateway/ui/controller/MainShellController.java`

Replace emoji icons with text:
```java
setNavState(navHomeIcon, navHomeLabel, "H", "Home", false);
setNavState(navSendIcon, navSendLabel, "S", "Send", false);
setNavState(navHistoryIcon, navHistoryLabel, "T", "History", false);
setNavState(navSettingsIcon, navSettingsLabel, "⚙", "Settings", false);
```

### Fix Notification Icons
**File:** `src/main/java/com/igirepay/igirepaypaymentgateway/ui/controller/NotificationsContentController.java`

Replace the `getNotificationIcon()` method:
```java
private String getNotificationIcon(String type) {
    return switch (type) {
        case Notification.TYPE_SENT -> "[→]";
        case Notification.TYPE_RECEIVED -> "[←]";
        case Notification.TYPE_DEPOSIT -> "[$]";
        case Notification.TYPE_WITHDRAWAL -> "[-]";
        case Notification.TYPE_ADMIN_MESSAGE -> "[!]";
        case Notification.TYPE_PASSWORD_RESET -> "[*]";
        case Notification.TYPE_LOAN_APPROVED -> "[+]";
        case Notification.TYPE_DISPUTE_UPDATE -> "[?]";
        case Notification.TYPE_ACCOUNT_UNLOCKED -> "[✓]";
        case Notification.TYPE_LOAN_REMINDER -> "[R]";
        case Notification.TYPE_FROZEN_FUNDS -> "[F]";
        default -> "[i]";
    };
}
```

## Testing After Fix

1. Clean and recompile:
   ```bash
   mvn clean compile
   ```

2. Run the application:
   ```bash
   mvn javafx:run
   ```

3. Check that:
   - Window title displays correctly
   - Navigation icons are readable
   - Notification messages are clear
   - No garbled characters in UI

## Prevention

To prevent this in the future:

1. **Always use UTF-8 encoding** in your IDE
2. **Avoid emojis** in Java source code (use icon fonts or images instead)
3. **Use ASCII characters** for console output
4. **Test on different systems** before deployment

## Alternative: Use Icon Fonts

Instead of text/emoji, use icon fonts like FontAwesome:

1. Add FontAwesome to your project
2. Use CSS classes for icons:
   ```java
   Label icon = new Label();
   icon.getStyleClass().add("fa-home");
   ```

This is more professional and avoids encoding issues entirely.
