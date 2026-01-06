# 🔍 DIAGNOSTIC CHECKLIST - 403 Error

Run through this checklist to diagnose your 403 error:

## ✅ Pre-Flight Checks

### 1. Backend Status
- [ ] Backend is running on port 3000
- [ ] You can access `http://10.152.62.164:3000` in browser
- [ ] Your Mac and Windows laptop are on same network

### 2. App Status  
- [ ] App is installed and running
- [ ] You can see the login screen
- [ ] No connection errors when opening app

---

## 🧪 Test Procedure

### Test 1: Check Network Connection
1. Open browser on your Mac
2. Navigate to: `http://10.152.62.164:3000`
3. **Expected:** Should see backend response (not "Cannot connect")
4. **Result:** ✅ / ❌

### Test 2: Login and Check Token
1. Open app
2. Go to Settings → Logout (if already logged in)
3. Login with your credentials
4. **Check Android Studio Logcat** for:
   ```
   D/AuthRepository: Token received: YES
   D/LoginView: Role: admin
   ```
5. **Result:** Token received? ✅ / ❌
6. **Result:** Role is admin? ✅ / ❌

### Test 3: Check Token Expiration
1. Keep Logcat open
2. After login, go to "Kelola Produk"
3. Click "+" to add product
4. **Check Logcat** for:
   ```
   D/AddProductView: === TOKEN DEBUG INFO ===
   D/AddProductView:   - Is Expired: false
   D/AddProductView:   - Is Admin: true
   ```
5. **Result:** Token expired? ✅ false / ❌ true
6. **Result:** Is admin? ✅ true / ❌ false

### Test 4: Load Categories
1. Still on Add Product page
2. **Check Logcat** for:
   ```
   D/CategoryRepository: === GET ALL CATEGORIES ===
   D/CategoryRepository: API Response: 200, Success: true
   ```
3. **Result:** Categories loaded? ✅ 200 / ❌ 403

### Test 5: Compare with Friend's Laptop
1. Ask friend to check their token in browser console or backend logs
2. Compare token issue time (iat) and expiration (exp)
3. **Your token iat:** ____________
4. **Friend's token iat:** ____________
5. **Time difference:** ____________

---

## 📊 Results Analysis

### If Test 1 FAILS ❌
**Problem:** Network connection
**Solution:** 
- Check if backend is running
- Check if IP address is correct
- Try ping `10.152.62.164` from Mac terminal

### If Test 2 FAILS ❌  
**Problem:** Login issues
**Solution:**
- Check username/password
- Check backend logs for login errors
- Verify backend `/api/login` or `/api/auth/login` endpoint

### If Test 3 Shows "Is Expired: true" ❌
**Problem:** Token expired
**Solution:**
- **LOGOUT → LOGIN AGAIN**
- This is THE solution!

### If Test 3 Shows "Is Admin: false" ❌
**Problem:** User doesn't have admin role
**Solution:**
- Update user role in backend database
- Or create new admin account
- Backend needs to set `role: "admin"` in JWT

### If Test 4 Returns 403 ❌
**Problem:** Token is expired or invalid
**Solution:**
- **LOGOUT → LOGIN AGAIN**
- Get fresh token
- Should fix 403 immediately

---

## 🎯 Decision Tree

```
Start
  │
  ├─→ Can friend create product on Windows? 
  │   ├─→ YES → Problem is YOUR token (expired/invalid)
  │   │         └─→ SOLUTION: Logout → Login
  │   │
  │   └─→ NO → Problem is backend or permissions
  │             └─→ Check backend logs
  │
  └─→ Did you logout and login today?
      ├─→ YES → Check if token has admin role
      │         └─→ Role is "admin"? 
      │             ├─→ YES → Backend issue
      │             └─→ NO → Need admin permissions
      │
      └─→ NO → LOGOUT → LOGIN NOW ← **DO THIS FIRST!**
```

---

## 🔧 Quick Fix Commands

### Terminal Commands to Check Backend:

```bash
# Check if backend is reachable
ping 10.152.62.164

# Check if port 3000 is open
nc -zv 10.152.62.164 3000

# Test categories endpoint (replace TOKEN with your token)
curl -H "Authorization: Bearer YOUR_TOKEN" http://10.152.62.164:3000/api/categories
```

### Android Studio Logcat Filters:

```
# Filter for authentication logs
Tag: AuthRepository OR LoginView

# Filter for category logs  
Tag: CategoryRepository

# Filter for token debug
Tag: AddProductView
```

---

## 📝 Report Template

If you need to share details with someone, use this:

```
### 403 Error Report

**Date:** December 15, 2025
**Time:** __________
**Device:** Mac

**Test Results:**
- Network connection: ✅ / ❌
- Login successful: ✅ / ❌  
- Token received: ✅ / ❌
- User role: admin / user / unknown
- Token expired: yes / no
- Categories API response: 200 / 403 / other: ____

**Friend's Device (Windows):**
- Can create product: ✅ / ❌
- Logged in at: __________

**Logcat Errors:**
```
[paste relevant logs here]
```

**Backend Logs:**
```
[paste backend errors if any]
```

**What I tried:**
1. [ ] Logout → Login
2. [ ] Restart app
3. [ ] Check backend running
4. [ ] Check network connection

**Current Status:** 
[Describe current situation]
```

---

## 🎯 Most Likely Solution

Based on "friend's Windows laptop works":

1. **LOGOUT** from your app
2. **LOGIN** again  
3. **Try creating product**
4. **Should work!** ✅

**Why?** Your token expired. Friend has fresh token. Simple as that!

---

## ✅ Success Criteria

You'll know it's fixed when you see:

```
D/LoginView: === LOGIN SUCCESS ===
D/LoginView: Role: admin
D/LoginView: Is Admin: true

D/AddProductView: - Is Expired: false
D/AddProductView: - Is Admin: true

D/CategoryRepository: API Response: 200, Success: true
D/CategoryRepository: Categories data: X items

✅ Product created successfully!
```

---

## 🆘 Emergency Contacts

If nothing works:

1. **Share full Logcat output** (from login to 403 error)
2. **Share backend logs** (what backend sees when you make request)
3. **Compare tokens** (yours vs friend's - check expiration times)
4. **Check JWT payload** (decode on jwt.io)

---

**Remember:** Since friend's laptop works, backend is fine. Problem is YOUR TOKEN. Logout → Login = Fixed! 🎉

