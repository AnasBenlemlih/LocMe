import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:locme/providers/auth_provider.dart';
import 'package:locme/providers/reservation_provider.dart';
import 'package:locme/models/reservation.dart';
import 'package:locme/models/user.dart';
import 'package:locme/widgets/custom_button.dart';

class SocieteReservationsScreen extends StatefulWidget {
  const SocieteReservationsScreen({super.key});

  @override
  State<SocieteReservationsScreen> createState() => _SocieteReservationsScreenState();
}

class _SocieteReservationsScreenState extends State<SocieteReservationsScreen> {
  final TextEditingController _searchController = TextEditingController();
  StatutReservation? _selectedStatus;
  List<Reservation> _filteredReservations = [];

  @override
  void initState() {
    super.initState();
    _loadReservations();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadReservations() async {
    final authProvider = context.read<AuthProvider>();
    final reservationProvider = context.read<ReservationProvider>();
    
    if (authProvider.user != null) {
      // For now, we'll use the user ID as société ID
      // In a real app, you'd get the société ID from the user's société relationship
      await reservationProvider.loadReservationsForSociete(authProvider.user!.id);
      _applyFilters();
    }
  }

  void _applyFilters() {
    final reservationProvider = context.read<ReservationProvider>();
    List<Reservation> filtered = reservationProvider.reservations;

    // Apply status filter
    if (_selectedStatus != null) {
      filtered = reservationProvider.getFilteredReservations(_selectedStatus);
    }

    // Apply search filter
    if (_searchController.text.isNotEmpty) {
      filtered = reservationProvider.searchReservations(_searchController.text);
    }

    setState(() {
      _filteredReservations = filtered;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final authProvider = context.watch<AuthProvider>();
    final reservationProvider = context.watch<ReservationProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Gestion des Réservations'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadReservations,
          ),
        ],
      ),
      body: Column(
        children: [
          // Statistics Cards
          _buildStatsCards(reservationProvider),
          
          // Search and Filter Bar
          _buildSearchAndFilterBar(),
          
          // Reservations List
          Expanded(
            child: reservationProvider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : reservationProvider.error != null
                    ? _buildErrorWidget(reservationProvider.error!)
                    : _filteredReservations.isEmpty
                        ? _buildEmptyWidget()
                        : _buildReservationsList(),
          ),
        ],
      ),
    );
  }

  Widget _buildStatsCards(ReservationProvider provider) {
    final stats = provider.getSocieteStats();
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: _StatCard(
              title: 'Total',
              value: stats['total']!.toString(),
              color: theme.colorScheme.primary,
              icon: Icons.calendar_today,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: _StatCard(
              title: 'En Attente',
              value: stats['pending']!.toString(),
              color: Colors.orange,
              icon: Icons.pending,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: _StatCard(
              title: 'Confirmées',
              value: stats['confirmed']!.toString(),
              color: Colors.blue,
              icon: Icons.check_circle,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: _StatCard(
              title: 'Terminées',
              value: stats['completed']!.toString(),
              color: Colors.green,
              icon: Icons.done_all,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchAndFilterBar() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Column(
        children: [
          // Search Bar
          TextField(
            controller: _searchController,
            decoration: InputDecoration(
              hintText: 'Rechercher par client ou modèle de voiture...',
              prefixIcon: const Icon(Icons.search),
              suffixIcon: _searchController.text.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.clear),
                      onPressed: () {
                        _searchController.clear();
                        _applyFilters();
                      },
                    )
                  : null,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            onChanged: (value) => _applyFilters(),
          ),
          const SizedBox(height: 12),
          
          // Status Filter
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                _FilterChip(
                  label: 'Tous',
                  isSelected: _selectedStatus == null,
                  onSelected: () {
                    setState(() {
                      _selectedStatus = null;
                    });
                    _applyFilters();
                  },
                ),
                const SizedBox(width: 8),
                ...StatutReservation.values.map((status) => Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: _FilterChip(
                    label: _getStatusDisplayName(status),
                    isSelected: _selectedStatus == status,
                    onSelected: () {
                      setState(() {
                        _selectedStatus = status;
                      });
                      _applyFilters();
                    },
                  ),
                )),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorWidget(String error) {
    return Center(
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
            'Erreur',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            error,
            style: Theme.of(context).textTheme.bodyMedium,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: _loadReservations,
            child: const Text('Réessayer'),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyWidget() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.calendar_today_outlined,
            size: 64,
            color: Colors.grey,
          ),
          const SizedBox(height: 16),
          Text(
            'Aucune réservation',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            'Aucune réservation trouvée pour vos voitures.',
            style: Theme.of(context).textTheme.bodyMedium,
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildReservationsList() {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _filteredReservations.length,
      itemBuilder: (context, index) {
        final reservation = _filteredReservations[index];
        return _ReservationCard(
          reservation: reservation,
          onConfirm: () => _handleConfirmReservation(reservation.id),
          onCancel: () => _handleCancelReservation(reservation.id),
          onComplete: () => _handleCompleteReservation(reservation.id),
        );
      },
    );
  }

  Future<void> _handleConfirmReservation(int reservationId) async {
    final reservationProvider = context.read<ReservationProvider>();
    final success = await reservationProvider.confirmReservation(reservationId);
    
    if (success) {
      _showSnackBar('Réservation confirmée avec succès', Colors.green);
      _applyFilters();
    } else {
      _showSnackBar('Erreur lors de la confirmation', Colors.red);
    }
  }

  Future<void> _handleCancelReservation(int reservationId) async {
    final confirmed = await _showConfirmDialog(
      'Annuler la réservation',
      'Êtes-vous sûr de vouloir annuler cette réservation ?',
    );
    
    if (confirmed) {
      final reservationProvider = context.read<ReservationProvider>();
      final success = await reservationProvider.cancelReservation(reservationId);
      
      if (success) {
        _showSnackBar('Réservation annulée avec succès', Colors.orange);
        _applyFilters();
      } else {
        _showSnackBar('Erreur lors de l\'annulation', Colors.red);
      }
    }
  }

  Future<void> _handleCompleteReservation(int reservationId) async {
    final confirmed = await _showConfirmDialog(
      'Marquer comme terminée',
      'Êtes-vous sûr de vouloir marquer cette réservation comme terminée ?',
    );
    
    if (confirmed) {
      final reservationProvider = context.read<ReservationProvider>();
      final success = await reservationProvider.completeReservation(reservationId);
      
      if (success) {
        _showSnackBar('Réservation marquée comme terminée', Colors.green);
        _applyFilters();
      } else {
        _showSnackBar('Erreur lors de la finalisation', Colors.red);
      }
    }
  }

  Future<bool> _showConfirmDialog(String title, String content) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(content),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Confirmer'),
          ),
        ],
      ),
    );
    return result ?? false;
  }

  void _showSnackBar(String message, Color color) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: color,
        duration: const Duration(seconds: 3),
      ),
    );
  }

  String _getStatusDisplayName(StatutReservation status) {
    switch (status) {
      case StatutReservation.EN_ATTENTE:
        return 'En Attente';
      case StatutReservation.CONFIRMEE:
        return 'Confirmée';
      case StatutReservation.EN_COURS:
        return 'En Cours';
      case StatutReservation.TERMINEE:
        return 'Terminée';
      case StatutReservation.ANNULEE:
        return 'Annulée';
      case StatutReservation.REFUSEE:
        return 'Refusée';
    }
  }
}

class _StatCard extends StatelessWidget {
  final String title;
  final String value;
  final Color color;
  final IconData icon;

  const _StatCard({
    required this.title,
    required this.value,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Icon(
              icon,
              color: color,
              size: 24,
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            Text(
              title,
              style: theme.textTheme.bodySmall,
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final bool isSelected;
  final VoidCallback onSelected;

  const _FilterChip({
    required this.label,
    required this.isSelected,
    required this.onSelected,
  });

  @override
  Widget build(BuildContext context) {
    return FilterChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (_) => onSelected(),
      selectedColor: Theme.of(context).colorScheme.primary.withOpacity(0.2),
      checkmarkColor: Theme.of(context).colorScheme.primary,
    );
  }
}

class _ReservationCard extends StatelessWidget {
  final Reservation reservation;
  final VoidCallback onConfirm;
  final VoidCallback onCancel;
  final VoidCallback onComplete;

  const _ReservationCard({
    required this.reservation,
    required this.onConfirm,
    required this.onCancel,
    required this.onComplete,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header with car info and status
            Row(
              children: [
                // Car image placeholder
                Container(
                  width: 60,
                  height: 60,
                  decoration: BoxDecoration(
                    color: theme.colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.car_rental,
                    size: 30,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${reservation.voiture.marque} ${reservation.voiture.modele}',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        '${reservation.voiture.annee} • ${reservation.voiture.kilometrage} km',
                        style: theme.textTheme.bodySmall,
                      ),
                    ],
                  ),
                ),
                _StatusChip(status: reservation.statut),
              ],
            ),
            
            const SizedBox(height: 16),
            
            // Client info
            Row(
              children: [
                const Icon(Icons.person, size: 16),
                const SizedBox(width: 8),
                Text(
                  'Client: ${reservation.user.nom}',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(width: 16),
                const Icon(Icons.email, size: 16),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    reservation.user.email,
                    style: theme.textTheme.bodySmall,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 8),
            
            // Dates
            Row(
              children: [
                const Icon(Icons.calendar_today, size: 16),
                const SizedBox(width: 8),
                Text(
                  'Du ${_formatDate(reservation.dateDebut)} au ${_formatDate(reservation.dateFin)}',
                  style: theme.textTheme.bodyMedium,
                ),
                const Spacer(),
                Text(
                  '${reservation.dureeEnJours} jour${reservation.dureeEnJours > 1 ? 's' : ''}',
                  style: theme.textTheme.bodySmall,
                ),
              ],
            ),
            
            const SizedBox(height: 8),
            
            // Price
            Row(
              children: [
                const Icon(Icons.euro, size: 16),
                const SizedBox(width: 8),
                Text(
                  'Prix total: ${reservation.montant.toStringAsFixed(2)} €',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: theme.colorScheme.primary,
                  ),
                ),
              ],
            ),
            
            // Comments if any
            if (reservation.commentaires != null && reservation.commentaires!.isNotEmpty) ...[
              const SizedBox(height: 8),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Icon(Icons.comment, size: 16),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      reservation.commentaires!,
                      style: theme.textTheme.bodySmall,
                    ),
                  ),
                ],
              ),
            ],
            
            const SizedBox(height: 16),
            
            // Action buttons
            _buildActionButtons(context),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButtons(BuildContext context) {
    final theme = Theme.of(context);
    
    switch (reservation.statut) {
      case StatutReservation.EN_ATTENTE:
        return Row(
          children: [
            Expanded(
              child: CustomButton(
                text: 'Confirmer',
                onPressed: onConfirm,
                backgroundColor: Colors.green,
                textColor: Colors.white,
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: CustomButton(
                text: 'Refuser',
                onPressed: onCancel,
                backgroundColor: Colors.red,
                textColor: Colors.white,
              ),
            ),
          ],
        );
      case StatutReservation.CONFIRMEE:
        return Row(
          children: [
            Expanded(
              child: CustomButton(
                text: 'Marquer Terminée',
                onPressed: onComplete,
                backgroundColor: theme.colorScheme.primary,
                textColor: Colors.white,
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: CustomButton(
                text: 'Annuler',
                onPressed: onCancel,
                backgroundColor: Colors.orange,
                textColor: Colors.white,
              ),
            ),
          ],
        );
      case StatutReservation.TERMINEE:
      case StatutReservation.ANNULEE:
      case StatutReservation.REFUSEE:
        return Container(
          padding: const EdgeInsets.symmetric(vertical: 8),
          child: Text(
            'Réservation ${reservation.statutDisplayName.toLowerCase()}',
            style: theme.textTheme.bodyMedium?.copyWith(
              fontStyle: FontStyle.italic,
              color: theme.colorScheme.onSurfaceVariant,
            ),
            textAlign: TextAlign.center,
          ),
        );
      default:
        return const SizedBox.shrink();
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
  }
}

class _StatusChip extends StatelessWidget {
  final StatutReservation status;

  const _StatusChip({required this.status});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    Color backgroundColor;
    Color textColor;
    
    switch (status) {
      case StatutReservation.EN_ATTENTE:
        backgroundColor = Colors.orange.withOpacity(0.2);
        textColor = Colors.orange;
        break;
      case StatutReservation.CONFIRMEE:
        backgroundColor = Colors.blue.withOpacity(0.2);
        textColor = Colors.blue;
        break;
      case StatutReservation.EN_COURS:
        backgroundColor = Colors.purple.withOpacity(0.2);
        textColor = Colors.purple;
        break;
      case StatutReservation.TERMINEE:
        backgroundColor = Colors.green.withOpacity(0.2);
        textColor = Colors.green;
        break;
      case StatutReservation.ANNULEE:
      case StatutReservation.REFUSEE:
        backgroundColor = Colors.red.withOpacity(0.2);
        textColor = Colors.red;
        break;
    }
    
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        _getStatusDisplayName(status),
        style: theme.textTheme.bodySmall?.copyWith(
          color: textColor,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  String _getStatusDisplayName(StatutReservation status) {
    switch (status) {
      case StatutReservation.EN_ATTENTE:
        return 'En Attente';
      case StatutReservation.CONFIRMEE:
        return 'Confirmée';
      case StatutReservation.EN_COURS:
        return 'En Cours';
      case StatutReservation.TERMINEE:
        return 'Terminée';
      case StatutReservation.ANNULEE:
        return 'Annulée';
      case StatutReservation.REFUSEE:
        return 'Refusée';
    }
  }
}

