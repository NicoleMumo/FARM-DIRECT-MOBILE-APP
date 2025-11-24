# Icon Replacement Guide

## Current Icon Usage
The `ic_seed` icon is used in **24+ places** throughout the app:
- Splash/Get Started screen (`activity_splash.xml`)
- Admin Dashboard headers and metric cards
- Farmer Dashboard headers
- Consumer Home screen
- Order Management screens
- Profile screens
- And many more...

## Option 1: Replace the Image File (EASIEST - Recommended)

**Steps:**
1. Get your new icon image (PNG format, recommended size: 512x512 or 256x256)
2. Replace the file: `app/src/main/res/drawable/ic_seed.png` with your new icon
3. Keep the same filename: `ic_seed.png`
4. Rebuild the app

**That's it!** All 24+ references will automatically use your new icon.

---

## Option 2: Use a Material Icon Instead

If you want to use a Material Icon (like a plant, leaf, or agriculture icon), you'll need to:

1. **For Compose screens** (most screens): Replace `painterResource(id = R.drawable.ic_seed)` with `Icons.Default.[IconName]`
2. **For XML layouts** (splash screen): You'll need to either:
   - Keep using the drawable file, OR
   - Convert the splash screen to Compose

**Available Material Icons for agriculture/farming:**
- `Icons.Default.Grass` (if available)
- `Icons.Default.Park` (tree/plant icon)
- `Icons.Default.Nature` (nature icon)
- Or any other Material Icon you prefer

**Note:** Material Icons may not have perfect agriculture-themed icons, so replacing the PNG file is usually the best option.

---

## Quick Reference: Files That Use ic_seed

### XML Layouts:
- `app/src/main/res/layout/activity_splash.xml` (Get Started page)

### Kotlin Files (Compose):
- `AdminDashboardScreen.kt`
- `OrderManagementScreen.kt`
- `FarmerDashboardScreen.kt`
- `ConsumerHomeScreen.kt`
- `FarmerProfileScreen.kt`
- `ProfileViewModel.kt`
- `CartViewModel.kt`
- `AdminDashboardViewModel.kt`
- And more...

---

## Recommendation

**Use Option 1** - Simply replace `ic_seed.png` with your new icon file. This is the fastest and most reliable method.


