import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:locme/providers/voiture_provider.dart';
import 'package:locme/models/voiture.dart';
import 'package:locme/widgets/voiture_card.dart';
import 'package:locme/routes.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final _searchController = TextEditingController();
  bool _showFilters = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<VoitureProvider>().loadAvailableVoitures();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final voitureProvider = Provider.of<VoitureProvider>(context);
    
    return Scaffold(
      appBar: AppBar(
        title: const Text('Rechercher'),
        actions: [
          IconButton(
            icon: Icon(_showFilters ? Icons.filter_list_off : Icons.filter_list),
            onPressed: () {
              setState(() {
                _showFilters = !_showFilters;
              });
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Search bar
          Container(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Rechercher par marque ou modèle...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          voitureProvider.searchVoitures('');
                        },
                      )
                    : null,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              onChanged: (value) {
                voitureProvider.searchVoitures(value);
              },
            ),
          ),
          
          // Filters section
          if (_showFilters)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Filtres',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 12),
                  _buildFilters(voitureProvider),
                  const SizedBox(height: 16),
                ],
              ),
            ),
          
          // Results
          Expanded(
            child: _buildResults(voitureProvider),
          ),
        ],
      ),
    );
  }

  Widget _buildFilters(VoitureProvider voitureProvider) {
    final theme = Theme.of(context);
    
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceVariant.withOpacity(0.3),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Filter chips for carburant
          Text(
            'Type de carburant',
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _FilterChip(
                label: 'Tous',
                isSelected: voitureProvider.selectedCarburant == null,
                onTap: () => voitureProvider.filterByCarburant(null),
              ),
              ...TypeCarburant.values.map((carburant) {
                return _FilterChip(
                  label: _getCarburantDisplayName(carburant),
                  isSelected: voitureProvider.selectedCarburant == carburant,
                  onTap: () => voitureProvider.filterByCarburant(carburant),
                );
              }),
            ],
          ),
          
          const SizedBox(height: 16),
          
          // Filter chips for transmission
          Text(
            'Transmission',
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _FilterChip(
                label: 'Tous',
                isSelected: voitureProvider.selectedTransmission == null,
                onTap: () => voitureProvider.filterByTransmission(null),
              ),
              ...TypeTransmission.values.map((transmission) {
                return _FilterChip(
                  label: _getTransmissionDisplayName(transmission),
                  isSelected: voitureProvider.selectedTransmission == transmission,
                  onTap: () => voitureProvider.filterByTransmission(transmission),
                );
              }),
            ],
          ),
          
          const SizedBox(height: 16),
          
          // Price range slider
          Text(
            'Prix maximum par jour',
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: Text(
                  '0€',
                  style: theme.textTheme.bodySmall,
                ),
              ),
              Expanded(
                flex: 3,
                child: Slider(
                  value: voitureProvider.maxPrice ?? 200.0,
                  min: 0,
                  max: 200,
                  divisions: 20,
                  label: '${(voitureProvider.maxPrice ?? 200.0).toInt()}€',
                  onChanged: (value) {
                    voitureProvider.filterByMaxPrice(value);
                  },
                ),
              ),
              Expanded(
                child: Text(
                  '200€',
                  style: theme.textTheme.bodySmall,
                  textAlign: TextAlign.end,
                ),
              ),
            ],
          ),
          
          const SizedBox(height: 16),
          
          // Clear filters button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: () {
                voitureProvider.clearFilters();
                _searchController.clear();
              },
              icon: const Icon(Icons.clear_all),
              label: const Text('Effacer tous les filtres'),
              style: ElevatedButton.styleFrom(
                backgroundColor: theme.colorScheme.errorContainer,
                foregroundColor: theme.colorScheme.onErrorContainer,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildResults(VoitureProvider voitureProvider) {
    final theme = Theme.of(context);
    
    if (voitureProvider.isLoading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }
    
    if (voitureProvider.error != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 64,
              color: theme.colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text(
              'Erreur de chargement',
              style: theme.textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(
              voitureProvider.error!,
              style: theme.textTheme.bodyMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => voitureProvider.loadAvailableVoitures(),
              child: const Text('Réessayer'),
            ),
          ],
        ),
      );
    }
    
    if (voitureProvider.voitures.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.search_off,
              size: 64,
              color: theme.colorScheme.onSurfaceVariant,
            ),
            const SizedBox(height: 16),
            Text(
              'Aucun résultat',
              style: theme.textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(
              'Aucune voiture ne correspond à vos critères de recherche.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                voitureProvider.clearFilters();
                _searchController.clear();
              },
              child: const Text('Effacer les filtres'),
            ),
          ],
        ),
      );
    }
    
    return RefreshIndicator(
      onRefresh: () => voitureProvider.loadAvailableVoitures(),
      child: GridView.builder(
        padding: const EdgeInsets.all(16),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          childAspectRatio: 0.75,
          crossAxisSpacing: 16,
          mainAxisSpacing: 16,
        ),
        itemCount: voitureProvider.voitures.length,
        itemBuilder: (context, index) {
          final voiture = voitureProvider.voitures[index];
          return VoitureCard(
            voiture: voiture,
            onTap: () => NavigationHelper.goToReservation(
              context,
              voitureId: voiture.id,
            ),
            showActions: true,
          );
        },
      ),
    );
  }

  String _getCarburantDisplayName(TypeCarburant carburant) {
    switch (carburant) {
      case TypeCarburant.ESSENCE:
        return 'Essence';
      case TypeCarburant.DIESEL:
        return 'Diesel';
      case TypeCarburant.HYBRIDE:
        return 'Hybride';
      case TypeCarburant.ELECTRIQUE:
        return 'Électrique';
      case TypeCarburant.GPL:
        return 'GPL';
    }
  }

  String _getTransmissionDisplayName(TypeTransmission transmission) {
    switch (transmission) {
      case TypeTransmission.MANUELLE:
        return 'Manuelle';
      case TypeTransmission.AUTOMATIQUE:
        return 'Automatique';
      case TypeTransmission.SEMI_AUTOMATIQUE:
        return 'Semi-automatique';
    }
  }
}

class _FilterChip extends StatelessWidget {
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterChip({
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected 
              ? theme.colorScheme.primary 
              : theme.colorScheme.surfaceVariant,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isSelected 
                ? theme.colorScheme.primary 
                : theme.colorScheme.outline,
            width: 1,
          ),
        ),
        child: Text(
          label,
          style: theme.textTheme.bodySmall?.copyWith(
            color: isSelected 
                ? theme.colorScheme.onPrimary 
                : theme.colorScheme.onSurfaceVariant,
            fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
          ),
        ),
      ),
    );
  }
}
