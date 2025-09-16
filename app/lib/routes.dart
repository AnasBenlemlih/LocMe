import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:locme/providers/auth_provider.dart';
import 'package:locme/screens/auth/login_screen.dart';
import 'package:locme/screens/auth/register_screen.dart';
import 'package:locme/screens/client/home_screen.dart';
import 'package:locme/screens/client/search_screen.dart';
import 'package:locme/screens/client/reservation_screen.dart';
import 'package:locme/screens/client/reservation_history_screen.dart';
import 'package:locme/screens/client/profile_screen.dart';
import 'package:locme/screens/societe/dashboard_screen.dart';
import 'package:locme/screens/societe/voitures_screen.dart';
import 'package:locme/screens/societe/reservations_screen.dart';
import 'package:locme/screens/admin/users_screen.dart';
import 'package:locme/screens/admin/societes_screen.dart';
import 'package:locme/screens/admin/stats_screen.dart';
import 'package:locme/models/user.dart';

class AppRoutes {
  static const String login = '/login';
  static const String register = '/register';
  static const String home = '/';
  static const String search = '/search';
  static const String reservation = '/reservation';
  static const String reservationHistory = '/reservations';
  static const String profile = '/profile';
  static const String societeDashboard = '/societe/dashboard';
  static const String societeVoitures = '/societe/voitures';
  static const String societeReservations = '/societe/reservations';
  static const String adminUsers = '/admin/users';
  static const String adminSocietes = '/admin/societes';
  static const String adminStats = '/admin/stats';
}

class AppRouter {
  static GoRouter get router => _router;

  static final GoRouter _router = GoRouter(
    initialLocation: AppRoutes.home,
    redirect: (context, state) {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      
      // If not logged in, redirect to login
      if (!authProvider.isLoggedIn) {
        return AppRoutes.login;
      }
      
      // If logged in, check role-based routing
      final user = authProvider.user;
      if (user != null) {
        final currentPath = state.uri.path;
        
        // Client routes
        if (user.isClient) {
          if (currentPath.startsWith('/admin') || currentPath.startsWith('/societe')) {
            return AppRoutes.home;
          }
        }
        
        // Société routes
        if (user.isSociete) {
          if (currentPath.startsWith('/admin') || 
              currentPath == AppRoutes.home || 
              currentPath == AppRoutes.search) {
            return AppRoutes.societeDashboard;
          }
        }
        
        // Admin routes
        if (user.isAdmin) {
          if (currentPath.startsWith('/societe') || 
              currentPath == AppRoutes.home || 
              currentPath == AppRoutes.search) {
            return AppRoutes.adminUsers;
          }
        }
      }
      
      return null; // No redirect needed
    },
    routes: [
      // Auth routes
      GoRoute(
        path: AppRoutes.login,
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: AppRoutes.register,
        builder: (context, state) => const RegisterScreen(),
      ),
      
      // Client routes
      GoRoute(
        path: AppRoutes.home,
        builder: (context, state) => const HomeScreen(),
      ),
      GoRoute(
        path: AppRoutes.search,
        builder: (context, state) => const SearchScreen(),
      ),
      GoRoute(
        path: AppRoutes.reservation,
        builder: (context, state) {
          final voitureId = state.uri.queryParameters['voitureId'];
          return ReservationScreen(voitureId: voitureId != null ? int.parse(voitureId) : null);
        },
      ),
      GoRoute(
        path: AppRoutes.reservationHistory,
        builder: (context, state) => const ReservationHistoryScreen(),
      ),
      GoRoute(
        path: AppRoutes.profile,
        builder: (context, state) => const ProfileScreen(),
      ),
      
      // Société routes
      GoRoute(
        path: AppRoutes.societeDashboard,
        builder: (context, state) => const SocieteDashboardScreen(),
      ),
      GoRoute(
        path: AppRoutes.societeVoitures,
        builder: (context, state) => const SocieteVoituresScreen(),
      ),
      GoRoute(
        path: AppRoutes.societeReservations,
        builder: (context, state) => const SocieteReservationsScreen(),
      ),
      
      // Admin routes
      GoRoute(
        path: AppRoutes.adminUsers,
        builder: (context, state) => const AdminUsersScreen(),
      ),
      GoRoute(
        path: AppRoutes.adminSocietes,
        builder: (context, state) => const AdminSocietesScreen(),
      ),
      GoRoute(
        path: AppRoutes.adminStats,
        builder: (context, state) => const AdminStatsScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.error_outline,
              size: 64,
              color: Colors.red,
            ),
            const SizedBox(height: 16),
            Text(
              'Page not found',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            Text(
              'The page you are looking for does not exist.',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => context.go(AppRoutes.home),
              child: const Text('Go Home'),
            ),
          ],
        ),
      ),
    ),
  );
}

// Navigation helpers
class NavigationHelper {
  static void goToLogin(BuildContext context) {
    context.go(AppRoutes.login);
  }
  
  static void goToRegister(BuildContext context) {
    context.go(AppRoutes.register);
  }
  
  static void goToHome(BuildContext context) {
    context.go(AppRoutes.home);
  }
  
  static void goToSearch(BuildContext context) {
    context.go(AppRoutes.search);
  }
  
  static void goToReservation(BuildContext context, {int? voitureId}) {
    if (voitureId != null) {
      context.go('${AppRoutes.reservation}?voitureId=$voitureId');
    } else {
      context.go(AppRoutes.reservation);
    }
  }
  
  static void goToReservationHistory(BuildContext context) {
    context.go(AppRoutes.reservationHistory);
  }
  
  static void goToProfile(BuildContext context) {
    context.go(AppRoutes.profile);
  }
  
  static void goToSocieteDashboard(BuildContext context) {
    context.go(AppRoutes.societeDashboard);
  }
  
  static void goToSocieteVoitures(BuildContext context) {
    context.go(AppRoutes.societeVoitures);
  }
  
  static void goToSocieteReservations(BuildContext context) {
    context.go(AppRoutes.societeReservations);
  }
  
  static void goToAdminUsers(BuildContext context) {
    context.go(AppRoutes.adminUsers);
  }
  
  static void goToAdminSocietes(BuildContext context) {
    context.go(AppRoutes.adminSocietes);
  }
  
  static void goToAdminStats(BuildContext context) {
    context.go(AppRoutes.adminStats);
  }
  
  // Role-based navigation
  static void navigateBasedOnRole(BuildContext context, User user) {
    if (user.isClient) {
      goToHome(context);
    } else if (user.isSociete) {
      goToSocieteDashboard(context);
    } else if (user.isAdmin) {
      goToAdminUsers(context);
    }
  }
}
