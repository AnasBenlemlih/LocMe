import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:locme/models/reservation.dart';

class ReservationCard extends StatelessWidget {
  final Reservation reservation;
  final VoidCallback? onTap;
  final bool showActions;
  final Function(StatutReservation)? onStatusChanged;

  const ReservationCard({
    super.key,
    required this.reservation,
    this.onTap,
    this.showActions = false,
    this.onStatusChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('dd/MM/yyyy');
    
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header with status
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      reservation.voiture.fullName,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  _StatusChip(status: reservation.statut),
                ],
              ),
              
              const SizedBox(height: 12),
              
              // Dates
              Row(
                children: [
                  Icon(
                    Icons.calendar_today,
                    size: 16,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '${dateFormat.format(reservation.dateDebut)} - ${dateFormat.format(reservation.dateFin)}',
                    style: theme.textTheme.bodyMedium,
                  ),
                ],
              ),
              
              const SizedBox(height: 8),
              
              // Duration and amount
              Row(
                children: [
                  Icon(
                    Icons.access_time,
                    size: 16,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '${reservation.dureeEnJours} jour${reservation.dureeEnJours > 1 ? 's' : ''}',
                    style: theme.textTheme.bodyMedium,
                  ),
                  const Spacer(),
                  Text(
                    '${reservation.montant.toStringAsFixed(0)}€',
                    style: theme.textTheme.titleMedium?.copyWith(
                      color: theme.colorScheme.primary,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              
              // Société info
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(
                    Icons.business,
                    size: 16,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      reservation.voiture.societe.nom,
                      style: theme.textTheme.bodyMedium,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
              
              // Pickup and return locations
              if (reservation.lieuPrise != null || reservation.lieuRetour != null) ...[
                const SizedBox(height: 8),
                if (reservation.lieuPrise != null)
                  Row(
                    children: [
                      Icon(
                        Icons.location_on,
                        size: 16,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'Prise: ${reservation.lieuPrise}',
                          style: theme.textTheme.bodySmall,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                if (reservation.lieuRetour != null)
                  Row(
                    children: [
                      Icon(
                        Icons.location_on,
                        size: 16,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'Retour: ${reservation.lieuRetour}',
                          style: theme.textTheme.bodySmall,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
              ],
              
              // Comments
              if (reservation.commentaires != null && reservation.commentaires!.isNotEmpty) ...[
                const SizedBox(height: 8),
                Text(
                  'Commentaires: ${reservation.commentaires}',
                  style: theme.textTheme.bodySmall,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              
              // Actions
              if (showActions && onStatusChanged != null) ...[
                const SizedBox(height: 16),
                _buildActionButtons(context),
              ],
            ],
          ),
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
              child: OutlinedButton(
                onPressed: () => onStatusChanged?.call(StatutReservation.REFUSEE),
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.red,
                  side: const BorderSide(color: Colors.red),
                ),
                child: const Text('Refuser'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ElevatedButton(
                onPressed: () => onStatusChanged?.call(StatutReservation.CONFIRMEE),
                child: const Text('Confirmer'),
              ),
            ),
          ],
        );
      
      case StatutReservation.CONFIRMEE:
        return Row(
          children: [
            Expanded(
              child: OutlinedButton(
                onPressed: () => onStatusChanged?.call(StatutReservation.ANNULEE),
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.orange,
                  side: const BorderSide(color: Colors.orange),
                ),
                child: const Text('Annuler'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ElevatedButton(
                onPressed: () => onStatusChanged?.call(StatutReservation.EN_COURS),
                child: const Text('Démarrer'),
              ),
            ),
          ],
        );
      
      case StatutReservation.EN_COURS:
        return SizedBox(
          width: double.infinity,
          child: ElevatedButton(
            onPressed: () => onStatusChanged?.call(StatutReservation.TERMINEE),
            child: const Text('Terminer'),
          ),
        );
      
      default:
        return const SizedBox.shrink();
    }
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
        backgroundColor = Colors.orange.withOpacity(0.1);
        textColor = Colors.orange;
        break;
      case StatutReservation.CONFIRMEE:
        backgroundColor = Colors.blue.withOpacity(0.1);
        textColor = Colors.blue;
        break;
      case StatutReservation.EN_COURS:
        backgroundColor = Colors.green.withOpacity(0.1);
        textColor = Colors.green;
        break;
      case StatutReservation.TERMINEE:
        backgroundColor = Colors.grey.withOpacity(0.1);
        textColor = Colors.grey;
        break;
      case StatutReservation.ANNULEE:
        backgroundColor = Colors.red.withOpacity(0.1);
        textColor = Colors.red;
        break;
      case StatutReservation.REFUSEE:
        backgroundColor = Colors.red.withOpacity(0.1);
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
        status.name.replaceAll('_', ' ').toLowerCase(),
        style: theme.textTheme.bodySmall?.copyWith(
          color: textColor,
          fontWeight: FontWeight.w500,
          fontSize: 11,
        ),
      ),
    );
  }
}

class ReservationListTile extends StatelessWidget {
  final Reservation reservation;
  final VoidCallback? onTap;
  final bool showActions;
  final Function(StatutReservation)? onStatusChanged;

  const ReservationListTile({
    super.key,
    required this.reservation,
    this.onTap,
    this.showActions = false,
    this.onStatusChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('dd/MM/yyyy');
    
    return ListTile(
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor: theme.colorScheme.primary.withOpacity(0.1),
        child: Icon(
          Icons.car_rental,
          color: theme.colorScheme.primary,
        ),
      ),
      title: Text(
        reservation.voiture.fullName,
        style: theme.textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.w600,
        ),
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '${dateFormat.format(reservation.dateDebut)} - ${dateFormat.format(reservation.dateFin)}',
            style: theme.textTheme.bodyMedium,
          ),
          Text(
            '${reservation.dureeEnJours} jour${reservation.dureeEnJours > 1 ? 's' : ''} • ${reservation.montant.toStringAsFixed(0)}€',
            style: theme.textTheme.bodySmall,
          ),
        ],
      ),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          _StatusChip(status: reservation.statut),
          if (showActions && onStatusChanged != null) ...[
            const SizedBox(height: 4),
            _buildQuickAction(context),
          ],
        ],
      ),
    );
  }

  Widget _buildQuickAction(BuildContext context) {
    switch (reservation.statut) {
      case StatutReservation.EN_ATTENTE:
        return IconButton(
          onPressed: () => onStatusChanged?.call(StatutReservation.CONFIRMEE),
          icon: const Icon(Icons.check, color: Colors.green),
          tooltip: 'Confirmer',
        );
      case StatutReservation.CONFIRMEE:
        return IconButton(
          onPressed: () => onStatusChanged?.call(StatutReservation.EN_COURS),
          icon: const Icon(Icons.play_arrow, color: Colors.blue),
          tooltip: 'Démarrer',
        );
      case StatutReservation.EN_COURS:
        return IconButton(
          onPressed: () => onStatusChanged?.call(StatutReservation.TERMINEE),
          icon: const Icon(Icons.stop, color: Colors.orange),
          tooltip: 'Terminer',
        );
      default:
        return const SizedBox.shrink();
    }
  }
}



