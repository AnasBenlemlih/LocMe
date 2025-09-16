import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:locme/models/voiture.dart';
import 'package:locme/routes.dart';

class VoitureCard extends StatelessWidget {
  final Voiture voiture;
  final VoidCallback? onTap;
  final bool showSocieteInfo;
  final bool showActions;

  const VoitureCard({
    super.key,
    required this.voiture,
    this.onTap,
    this.showSocieteInfo = true,
    this.showActions = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image
            ClipRRect(
              borderRadius: const BorderRadius.vertical(top: Radius.circular(12)),
              child: AspectRatio(
                aspectRatio: 16 / 9,
                child: voiture.imageUrl != null
                    ? CachedNetworkImage(
                        imageUrl: voiture.imageUrl!,
                        fit: BoxFit.cover,
                        placeholder: (context, url) => Container(
                          color: theme.colorScheme.surfaceVariant,
                          child: const Center(
                            child: CircularProgressIndicator(),
                          ),
                        ),
                        errorWidget: (context, url, error) => Container(
                          color: theme.colorScheme.surfaceVariant,
                          child: Icon(
                            Icons.car_rental,
                            size: 48,
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      )
                    : Container(
                        color: theme.colorScheme.surfaceVariant,
                        child: Icon(
                          Icons.car_rental,
                          size: 48,
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
              ),
            ),
            
            // Content
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Title and price
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(
                          voiture.fullName,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Text(
                        '${voiture.prixParJour.toStringAsFixed(0)}€/jour',
                        style: theme.textTheme.titleMedium?.copyWith(
                          color: theme.colorScheme.primary,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                  
                  const SizedBox(height: 8),
                  
                  // Year and mileage
                  if (voiture.annee != null || voiture.kilometrage != null)
                    Row(
                      children: [
                        if (voiture.annee != null) ...[
                          Icon(
                            Icons.calendar_today,
                            size: 16,
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            voiture.annee.toString(),
                            style: theme.textTheme.bodySmall,
                          ),
                        ],
                        if (voiture.annee != null && voiture.kilometrage != null)
                          const SizedBox(width: 16),
                        if (voiture.kilometrage != null) ...[
                          Icon(
                            Icons.speed,
                            size: 16,
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            '${voiture.kilometrage.toString()} km',
                            style: theme.textTheme.bodySmall,
                          ),
                        ],
                      ],
                    ),
                  
                  const SizedBox(height: 8),
                  
                  // Features
                  Wrap(
                    spacing: 8,
                    runSpacing: 4,
                    children: [
                      if (voiture.carburant != null)
                        _FeatureChip(
                          icon: Icons.local_gas_station,
                          label: voiture.carburantDisplayName,
                        ),
                      if (voiture.transmission != null)
                        _FeatureChip(
                          icon: Icons.settings,
                          label: voiture.transmissionDisplayName,
                        ),
                      if (voiture.nombrePlaces != null)
                        _FeatureChip(
                          icon: Icons.person,
                          label: '${voiture.nombrePlaces} places',
                        ),
                    ],
                  ),
                  
                  // Société info
                  if (showSocieteInfo) ...[
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Icon(
                          Icons.business,
                          size: 16,
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                        const SizedBox(width: 4),
                        Expanded(
                          child: Text(
                            voiture.societe.nom,
                            style: theme.textTheme.bodySmall?.copyWith(
                              fontWeight: FontWeight.w500,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  ],
                  
                  // Availability status
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Container(
                        width: 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: voiture.disponible 
                              ? Colors.green 
                              : Colors.red,
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        voiture.disponible ? 'Disponible' : 'Indisponible',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: voiture.disponible 
                              ? Colors.green 
                              : Colors.red,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                  
                  // Actions
                  if (showActions) ...[
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton(
                            onPressed: () {
                              // Navigate to reservation
                              NavigationHelper.goToReservation(
                                context,
                                voitureId: voiture.id,
                              );
                            },
                            child: const Text('Réserver'),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: ElevatedButton(
                            onPressed: () {
                              // Show details
                              _showVoitureDetails(context, voiture);
                            },
                            child: const Text('Détails'),
                          ),
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showVoitureDetails(BuildContext context, Voiture voiture) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        maxChildSize: 0.9,
        minChildSize: 0.5,
        expand: false,
        builder: (context, scrollController) => VoitureDetailsSheet(
          voiture: voiture,
          scrollController: scrollController,
        ),
      ),
    );
  }
}

class _FeatureChip extends StatelessWidget {
  final IconData icon;
  final String label;

  const _FeatureChip({
    required this.icon,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceVariant,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            icon,
            size: 12,
            color: theme.colorScheme.onSurfaceVariant,
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(
              fontSize: 11,
            ),
          ),
        ],
      ),
    );
  }
}

class VoitureDetailsSheet extends StatelessWidget {
  final Voiture voiture;
  final ScrollController scrollController;

  const VoitureDetailsSheet({
    super.key,
    required this.voiture,
    required this.scrollController,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Container(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Handle bar
          Center(
            child: Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: theme.colorScheme.onSurfaceVariant,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),
          
          const SizedBox(height: 20),
          
          // Title
          Text(
            voiture.fullName,
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          
          const SizedBox(height: 8),
          
          // Price
          Text(
            '${voiture.prixParJour.toStringAsFixed(0)}€ par jour',
            style: theme.textTheme.titleMedium?.copyWith(
              color: theme.colorScheme.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
          
          const SizedBox(height: 20),
          
          // Details
          Expanded(
            child: SingleChildScrollView(
              controller: scrollController,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _DetailRow(
                    icon: Icons.business,
                    label: 'Société',
                    value: voiture.societe.nom,
                  ),
                  
                  if (voiture.annee != null)
                    _DetailRow(
                      icon: Icons.calendar_today,
                      label: 'Année',
                      value: voiture.annee.toString(),
                    ),
                  
                  if (voiture.kilometrage != null)
                    _DetailRow(
                      icon: Icons.speed,
                      label: 'Kilométrage',
                      value: '${voiture.kilometrage.toString()} km',
                    ),
                  
                  if (voiture.carburant != null)
                    _DetailRow(
                      icon: Icons.local_gas_station,
                      label: 'Carburant',
                      value: voiture.carburantDisplayName,
                    ),
                  
                  if (voiture.transmission != null)
                    _DetailRow(
                      icon: Icons.settings,
                      label: 'Transmission',
                      value: voiture.transmissionDisplayName,
                    ),
                  
                  if (voiture.nombrePlaces != null)
                    _DetailRow(
                      icon: Icons.person,
                      label: 'Nombre de places',
                      value: voiture.nombrePlaces.toString(),
                    ),
                  
                  if (voiture.description != null) ...[
                    const SizedBox(height: 16),
                    Text(
                      'Description',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      voiture.description!,
                      style: theme.textTheme.bodyMedium,
                    ),
                  ],
                ],
              ),
            ),
          ),
          
          // Action button
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                NavigationHelper.goToReservation(
                  context,
                  voitureId: voiture.id,
                );
              },
              child: const Text('Réserver maintenant'),
            ),
          ),
        ],
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _DetailRow({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Icon(
            icon,
            size: 20,
            color: theme.colorScheme.primary,
          ),
          const SizedBox(width: 12),
          Text(
            '$label: ',
            style: theme.textTheme.bodyMedium?.copyWith(
              fontWeight: FontWeight.w500,
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: theme.textTheme.bodyMedium,
            ),
          ),
        ],
      ),
    );
  }
}
