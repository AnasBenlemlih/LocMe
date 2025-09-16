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
    return Column(
      children: [
        // Carburant filter
        Row(
          children: [
            Expanded(
              child: DropdownButtonFormField<TypeCarburant?>(
                value: voitureProvider.selectedCarburant,
                decoration: const InputDecoration(
                  labelText: 'Carburant',
                  border: OutlineInputBorder(),
                  contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                items: [
                  const DropdownMenuItem(
                    value: null,
                    child: Text('Tous'),
                  ),
                  ...TypeCarburant.values.map((carburant) {
                    return DropdownMenuItem(
                      value: carburant,
                      child: Text(_getCarburantDisplayName(carburant)),
                    );
                  }),
                ],
                onChanged: (value) {
                  voitureProvider.filterByCarburant(value);
                },
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: DropdownButtonFormField<TypeTransmission?>(
                value: voitureProvider.selectedTransmission,
                decoration: const InputDecoration(
                  labelText: 'Transmission',
                  border: OutlineInputBorder(),
                  contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                items: [
                  const DropdownMenuItem(
                    value: null,
                    child: Text('Tous'),
                  ),
                  ...TypeTransmission.values.map((transmission) {
                    return DropdownMenuItem(
                      value: transmission,
                      child: Text(_getTransmissionDisplayName(transmission)),
                    );
                  }),
                ],
                onChanged: (value) {
                  voitureProvider.filterByTransmission(value);
                },
              ),
            ),
          ],
        ),
        
        const SizedBox(height: 12),
        
        // Price and places filters
        Row(
          children: [
            Expanded(
              child: TextFormField(
                decoration: const InputDecoration(
                  labelText: 'Prix max (€/jour)',
                  border: OutlineInputBorder(),
                  contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                keyboardType: TextInputType.number,
                onChanged: (value) {
                  final price = double.tryParse(value);
                  voitureProvider.filterByMaxPrice(price);
                },
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                decoration: const InputDecoration(
                  labelText: 'Places min',
                  border: OutlineInputBorder(),
                  contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                keyboardType: TextInputType.number,
                onChanged: (value) {
                  final places = int.tryParse(value);
                  voitureProvider.filterByMinPlaces(places);
                },
              ),
            ),
          ],
        ),
        
        const SizedBox(height: 12),
        
        // Clear filters button
        SizedBox(
          width: double.infinity,
          child: OutlinedButton(
            onPressed: () {
              voitureProvider.clearFilters();
              _searchController.clear();
            },
            child: const Text('Effacer les filtres'),
          ),
        ),
      ],
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
