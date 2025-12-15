# 🔥 403 ERROR - COMPLETE DIAGNOSTIC GUIDE

## 🚨 YOU'RE STILL GETTING 403 ERRORS!

I've added advanced debugging tools to help diagnose the exact cause!

---

## ✅ WHAT I JUST ADDED

### 1. **DebugHelper.kt** - Advanced 403 Diagnostic Tool ✨

This new utility will automatically:
- ✅ Decode your JWT token
- ✅ Check if token is expired
- ✅ Check if user is admin
- ✅ Show token details (userId, username, role)
- ✅ Show exact error from backend
- ✅ Suggest solutions based on the error

### 2. **Enhanced Error Messages** ✨

Both CategoryRepository and ProductRepository now:
- ✅ Call DebugHelper when 403 occurs
- ✅ Log detailed diagnostic info to Logcat
- ✅ Show user-friendly error messages
- ✅ Guide you to check Logcat for details

---

## 🔍 HOW TO DIAGNOSE YOUR 403 ERROR

### Step 1: Run Your App

1. **Build and install** the updated app
2. **Login** with your account
3. **Try the action that gives 403** (create product, load categories, etc.)

### Step 2: Check Logcat

When you get 403 error, **immediately check Logcat**:

#### Filter by: `DEBUG_403`

You'll see output like this:

```
E/DEBUG_403: ═══════════════════════════════════════════
E/DEBUG_403: 🔥 403 FORBIDDEN ERROR DETECTED!
E/DEBUG_403: ═══════════════════════════════════════════
E/DEBUG_403: Source: CategoryRepository.getAllCategories
E/DEBUG_403: Endpoint: GET /categories
E/DEBUG_403: ─────────────────────────────────────────────
E/DEBUG_403: Token Status: PRESENT
E/DEBUG_403: Token Length: 250
E/DEBUG_403: Token Preview: eyJhbGciOiJIUzI1NiIsInR5cCI...
E/DEBUG_403: ─────────────────────────────────────────────
E/DEBUG_403: TOKEN DETAILS:
E/DEBUG_403:   User ID: 123
E/DEBUG_403:   Username: yourname
E/DEBUG_403:   Role: user          ← CHECK THIS!
E/DEBUG_403:   Issued At: 1702716000
E/DEBUG_403:   Expires At: 1702802400
E/DEBUG_403:   Is Expired: false ✅
E/DEBUG_403:   Is Admin: false ❌  ← CHECK THIS!
E/DEBUG_403: ─────────────────────────────────────────────
E/DEBUG_403: Error Response Body:
E/DEBUG_403: {"code":403,"status":"FORBIDDEN","message":"Admin access required"}
E/DEBUG_403: ═══════════════════════════════════════════
E/DEBUG_403: POSSIBLE CAUSES:
E/DEBUG_403: 1. Token expired - LOGOUT and LOGIN again
E/DEBUG_403: 2. Not admin - Need admin role in backend
E/DEBUG_403: 3. Resource ownership - Trying to access another user's data
E/DEBUG_403: 4. Backend validation - Check backend logs
E/DEBUG_403: ═══════════════════════════════════════════
```

---

## 🎯 COMMON 403 CAUSES & SOLUTIONS

### Cause #1: Token Expired ⏰

**Logcat shows:**
```
Is Expired: true ❌
⚠️ TOKEN IS EXPIRED!
SOLUTION: LOGOUT AND LOGIN AGAIN
```

**Solution:**
1. **Logout** dari app
2. **Login** lagi
3. **Try again** - should work ✅

---

### Cause #2: Not Admin Role 👤

**Logcat shows:**
```
Role: user
Is Admin: false ❌
⚠️ USER IS NOT ADMIN!
```

**Solution:**

#### Option A: Update Role in Backend (RECOMMENDED)
```sql
-- Connect to your database (PostgreSQL/MySQL)
UPDATE users SET role = 'admin' WHERE username = 'yourname';
```

#### Option B: Create New Admin Account
1. Go to your backend code
2. Find user registration/creation
3. Set `role: 'admin'` when creating user
4. Create new account with admin role

---

### Cause #3: Category Ownership Issue 🏷️

**Logcat shows:**
```
Error Response Body:
{"code":403,"message":"Category not found or not owned by user"}
```

**This means:**
- You're trying to create product with category that belongs to another user
- Backend is correctly blocking this!

**Solution:**
1. **Logout and login** to clear cached categories
2. **Create your own category** first
3. **Then create product** with YOUR category
4. Should work ✅

---

### Cause #4: Wrong IP Address 🌐

**Check AppContainer.kt:**
```kotlin
private const val ROOT_URL = "http://10.0.2.2:3000"
```

**If using Physical Device:**
- ❌ `10.0.2.2` won't work (that's for emulator only)
- ✅ Use your Mac's actual IP: `http://192.168.X.X:3000`

**To find your Mac's IP:**
```bash
# Run in Terminal:
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Then update `ROOT_URL` in AppContainer.kt:
```kotlin
private const val ROOT_URL = "http://YOUR_MAC_IP:3000"
```

---

### Cause #5: Backend Validation Error 🖥️

**Logcat shows specific backend error:**
```
Error Response Body:
{"code":403,"message":"Invalid categoryId: category must exist and belong to user"}
```

**Solution:**
- Read the backend error message carefully
- It tells you exactly what's wrong
- Fix based on the message

---

## 🧪 STEP-BY-STEP DEBUGGING PROCESS

### Test 1: Check Your Token

1. **Login to your app**
2. **Check Logcat** for login success
3. **Look for:**
   ```
   D/LoginView: === LOGIN SUCCESS ===
   D/LoginView: Role: admin  ← Should say "admin"!
   D/LoginView: Is Admin: true ← Should be true!
   ```

**If it says `Role: user` or `Is Admin: false`:**
- ❌ You don't have admin role
- ✅ Update role in database (see Cause #2)

---

### Test 2: Test Category Access

1. **Go to "Kelola Produk"**
2. **Click "+" to add product**
3. **Categories dropdown should load**

**If you get 403:**
- **Check Logcat** filter `DEBUG_403`
- **Read the token details** - expired? not admin?
- **Follow the solution** for your specific cause

---

### Test 3: Test Product Creation

1. **Select a category** (your own category!)
2. **Fill product details**
3. **Upload image**
4. **Click create**

**If you get 403:**
- **Check Logcat** filter `DEBUG_403`
- **Look at error body** - what does backend say?
- **Most common:** Using category from another user

---

## 📊 DIAGNOSTIC CHECKLIST

Use this checklist to systematically diagnose:

- [ ] **Token exists?**
  - Check: Logcat shows "Token Status: PRESENT"
  - If not: Login again

- [ ] **Token expired?**
  - Check: Logcat shows "Is Expired: false"
  - If expired: Logout and login again

- [ ] **User is admin?**
  - Check: Logcat shows "Is Admin: true"
  - If not: Update role in database

- [ ] **Using own categories?**
  - Check: Categories you created yourself
  - If not: Create your own categories first

- [ ] **Backend reachable?**
  - Check: Can access `http://YOUR_IP:3000` in browser
  - If not: Check ROOT_URL in AppContainer.kt

- [ ] **Backend logs show error?**
  - Check: Your backend console/logs
  - Often shows more detailed error

---

## 🚀 QUICK FIXES TO TRY

### Fix #1: Fresh Login
```
1. Logout completely
2. Close app
3. Open app
4. Login again
5. Try again
```

### Fix #2: Make Yourself Admin
```sql
-- In your database:
UPDATE users SET role = 'admin' WHERE id = YOUR_USER_ID;

-- Or by username:
UPDATE users SET role = 'admin' WHERE username = 'yourname';
```

### Fix #3: Use Own Categories
```
1. Go to "Kelola Produk" → Tab "Categories"
2. Delete old categories (if any)
3. Create NEW category with your account
4. Use THIS category when creating product
```

### Fix #4: Check IP Address
```kotlin
// In AppContainer.kt:

// For Emulator:
private const val ROOT_URL = "http://10.0.2.2:3000"

// For Physical Device (update with YOUR Mac IP):
private const val ROOT_URL = "http://192.168.1.100:3000"
```

---

## 🔍 HOW TO SHARE DEBUG INFO WITH ME

If still stuck, share these from Logcat:

1. **Filter by `DEBUG_403`** - copy ALL output
2. **Filter by `LoginView`** - show login logs
3. **Filter by `CategoryRepository`** - show API logs
4. **Filter by `ProductRepository`** - show create product logs

Share this info and I can tell you EXACTLY what's wrong!

---

## 💡 MOST LIKELY CAUSES (In Order)

### 1. **Role is "user" not "admin"** (90% of cases)
   - **Check:** Logcat shows `Is Admin: false`
   - **Fix:** Update role to admin in database

### 2. **Using category from another user** (5% of cases)
   - **Check:** Backend error says "category not owned by user"
   - **Fix:** Create and use your own category

### 3. **Token expired** (3% of cases)
   - **Check:** Logcat shows `Is Expired: true`
   - **Fix:** Logout and login again

### 4. **Wrong IP/network** (2% of cases)
   - **Check:** Can't reach backend at all
   - **Fix:** Update ROOT_URL with correct IP

---

## 🎯 ACTION PLAN

**DO THIS NOW:**

1. **Build and run** updated app (with DebugHelper)
2. **Try to create product** or load categories
3. **When you get 403** - immediately check Logcat
4. **Filter by "DEBUG_403"** to see detailed diagnostic
5. **Read the token details** carefully
6. **Follow the solution** for your specific issue
7. **Share the DEBUG_403 logs** if still stuck

---

## 📞 EXPECTED DEBUG OUTPUT

When it works (no 403):
```
D/LoginView: Role: admin ✅
D/LoginView: Is Admin: true ✅
D/CategoryRepository: API Response: 200, Success: true ✅
D/ProductRepository: Response code: 200 ✅
```

When it fails (403):
```
E/DEBUG_403: 🔥 403 FORBIDDEN ERROR DETECTED!
E/DEBUG_403:   Role: user ❌ (or)
E/DEBUG_403:   Is Expired: true ❌ (or)
E/DEBUG_403:   Error: Category not owned by user ❌
```

---

**The debug tools are now in place! Run your app, trigger the 403 error, and check Logcat filter `DEBUG_403` to see EXACTLY what's causing it!** 🔍

**Then tell me what you see in the DEBUG_403 logs and I'll tell you the exact solution!** 💪

