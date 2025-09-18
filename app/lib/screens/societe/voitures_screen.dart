import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:locme/providers/auth_provider.dart';
import 'package:locme/providers/voiture_provider.dart';
import 'package:locme/models/voiture.dart';
import 'voitures_screen_widgets.dart';

class SocieteVoituresScreen extends StatefulWidget {
  const SocieteVoituresScreen({super.key});

  @override
  State<SocieteVoituresScreen> createState() => _SocieteVoituresScreenState();
}

class _SocieteVoituresScreenState extends State<SocieteVoituresScreen> {
  final TextEditingController _searchController = TextEditingController();
  bool? _availabilityFilter;
  List<Voiture> _filteredVoitures = [];

  @override
  void initState() {
    super.initState();
    _loadVoitures();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadVoitures() async {
    final authProvider = context.read<AuthProvider>();
    final voitureProvider = context.read<VoitureProvider>();
    
    if (authProvider.user != null) {
      await voitureProvider.loadVoituresForSociete(authProvider.user!.id);
      _applyFilters();
    }
  }

  void _applyFilters() {
    final voitureProvider = context.read<VoitureProvider>();
    List<Voiture> filtered = voitureProvider.voitures;

    // Apply availability filter
    if (_availabilityFilter != null) {
      filtered = filtered.where((v) => v.disponible == _availabilityFilter).toList();
    }

    // Apply search filter
    if (_searchController.text.isNotEmpty) {
      final query = _searchController.text.toLowerCase();
      filtered = filtered.where((v) {
        return v.marque.toLowerCase().contains(query) ||
               v.modele.toLowerCase().contains(query);
      }).toList();
    }

    setState(() {
      _filteredVoitures = filtered;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final authProvider = context.watch<AuthProvider>();
    final voitureProvider = context.watch<VoitureProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Gestion des Voitures'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadVoitures,
          ),
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () => _showAddVoitureDialog(),
          ),
        ],
      ),
      body: Column(
        children: [
          // Statistics Cards
          _buildStatsCards(voitureProvider),
          
          // Search and Filter Bar
          _buildSearchAndFilterBar(),
          
          // Voitures List
          Expanded(
            child: voitureProvider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : voitureProvider.error != null
                    ? _buildErrorWidget(voitureProvider.error!)
                    : _filteredVoitures.isEmpty
                        ? _buildEmptyWidget()
                        : _buildVoituresList(),
          ),
        ],
      ),
    );
  }

  Widget _buildStatsCards(VoitureProvider provider) {
    final stats = provider.getSocieteStats();
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: StatCard(
              title: 'Total',
              value: stats['total']!.toString(),
              color: theme.colorScheme.primary,
              icon: Icons.car_rental,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: StatCard(
              title: 'Disponibles',
              value: stats['available']!.toString(),
              color: Colors.green,
              icon: Icons.check_circle,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: StatCard(
              title: 'Indisponibles',
              value: stats['unavailable']!.toString(),
              color: Colors.red,
              icon: Icons.cancel,
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
              hintText: 'Rechercher par marque ou modèle...',
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
          
          // Availability Filter
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                CustomFilterChip(
                  label: 'Toutes',
                  isSelected: _availabilityFilter == null,
                  onSelected: () {
                    setState(() {
                      _availabilityFilter = null;
                    });
                    _applyFilters();
                  },
                ),
                const SizedBox(width: 8),
                CustomFilterChip(
                  label: 'Disponibles',
                  isSelected: _availabilityFilter == true,
                  onSelected: () {
                    setState(() {
                      _availabilityFilter = true;
                    });
                    _applyFilters();
                  },
                ),
                const SizedBox(width: 8),
                CustomFilterChip(
                  label: 'Indisponibles',
                  isSelected: _availabilityFilter == false,
                  onSelected: () {
                    setState(() {
                      _availabilityFilter = false;
                    });
                    _applyFilters();
                  },
                ),
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
            onPressed: _loadVoitures,
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
            Icons.car_rental_outlined,
            size: 64,
            color: Colors.grey,
          ),
          const SizedBox(height: 16),
          Text(
            'Aucune voiture',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            'Vous n\'avez pas encore de voitures dans votre flotte.',
            style: Theme.of(context).textTheme.bodyMedium,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          ElevatedButton.icon(
            onPressed: () => _showAddVoitureDialog(),
            icon: const Icon(Icons.add),
            label: const Text('Ajouter une voiture'),
          ),
        ],
      ),
    );
  }

  Widget _buildVoituresList() {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _filteredVoitures.length,
      itemBuilder: (context, index) {
        final voiture = _filteredVoitures[index];
        return VoitureCard(
          voiture: voiture,
          onEdit: () => _showEditVoitureDialog(voiture),
          onDelete: () => _handleDeleteVoiture(voiture.id),
          onToggleAvailability: () => _handleToggleAvailability(voiture),
        );
      },
    );
  }

  Future<void> _handleDeleteVoiture(int voitureId) async {
    final confirmed = await _showConfirmDialog(
      'Supprimer la voiture',
      'Êtes-vous sûr de vouloir supprimer cette voiture ? Cette action est irréversible.',
    );
    
    if (confirmed) {
      final voitureProvider = context.read<VoitureProvider>();
      final success = await voitureProvider.deleteVoiture(voitureId);
      
      if (success) {
        _showSnackBar('Voiture supprimée avec succès', Colors.green);
        _applyFilters();
      } else {
        _showSnackBar('Erreur lors de la suppression', Colors.red);
      }
    }
  }

  Future<void> _handleToggleAvailability(Voiture voiture) async {
    final voitureProvider = context.read<VoitureProvider>();
    final newAvailability = !voiture.disponible;
    final success = await voitureProvider.toggleVoitureAvailability(voiture.id, newAvailability);
    
    if (success) {
      final message = newAvailability ? 'Voiture marquée comme disponible' : 'Voiture marquée comme indisponible';
      _showSnackBar(message, Colors.green);
      _applyFilters();
    } else {
      _showSnackBar('Erreur lors de la mise à jour', Colors.red);
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

  void _showAddVoitureDialog() {
    showDialog(
      context: context,
      builder: (context) => _VoitureFormDialog(
        onSave: (voitureData) async {
          final voitureProvider = context.read<VoitureProvider>();
          final success = await voitureProvider.createVoiture(voitureData);
          
          if (success) {
            _showSnackBar('Voiture ajoutée avec succès', Colors.green);
            _applyFilters();
          } else {
            _showSnackBar('Erreur lors de l\'ajout', Colors.red);
          }
        },
      ),
    );
  }

  void _showEditVoitureDialog(Voiture voiture) {
    showDialog(
      context: context,
      builder: (context) => _VoitureFormDialog(
        voiture: voiture,
        onSave: (voitureData) async {
          final voitureProvider = context.read<VoitureProvider>();
          final success = await voitureProvider.updateVoiture(voiture.id, voitureData);
          
          if (success) {
            _showSnackBar('Voiture mise à jour avec succès', Colors.green);
            _applyFilters();
          } else {
            _showSnackBar('Erreur lors de la mise à jour', Colors.red);
          }
        },
      ),
    );
  }
}

class _VoitureFormDialog extends StatefulWidget {
  final Voiture? voiture;
  final Function(Map<String, dynamic>) onSave;

  const _VoitureFormDialog({
    this.voiture,
    required this.onSave,
  });

  @override
  State<_VoitureFormDialog> createState() => _VoitureFormDialogState();
}

class _VoitureFormDialogState extends State<_VoitureFormDialog> {
  final _formKey = GlobalKey<FormState>();
  final _marqueController = TextEditingController();
  final _modeleController = TextEditingController();
  final _prixController = TextEditingController();
  final _anneeController = TextEditingController();
  final _kilometrageController = TextEditingController();
  final _placesController = TextEditingController();
  final _descriptionController = TextEditingController();
  
  bool _disponible = true;
  TypeCarburant? _carburant;
  TypeTransmission? _transmission;

  @override
  void initState() {
    super.initState();
    if (widget.voiture != null) {
      _marqueController.text = widget.voiture!.marque;
      _modeleController.text = widget.voiture!.modele;
      _prixController.text = widget.voiture!.prixParJour.toString();
      _anneeController.text = widget.voiture!.annee?.toString() ?? '';
      _kilometrageController.text = widget.voiture!.kilometrage?.toString() ?? '';
      _placesController.text = widget.voiture!.nombrePlaces?.toString() ?? '';
      _descriptionController.text = widget.voiture!.description ?? '';
      _disponible = widget.voiture!.disponible;
      _carburant = widget.voiture!.carburant;
      _transmission = widget.voiture!.transmission;
    }
  }

  @override
  void dispose() {
    _marqueController.dispose();
    _modeleController.dispose();
    _prixController.dispose();
    _anneeController.dispose();
    _kilometrageController.dispose();
    _placesController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isEditing = widget.voiture != null;

    return AlertDialog(
      title: Text(isEditing ? 'Modifier la voiture' : 'Ajouter une voiture'),
      content: SizedBox(
        width: double.maxFinite,
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Marque
                TextFormField(
                  controller: _marqueController,
                  decoration: const InputDecoration(
                    labelText: 'Marque *',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'La marque est obligatoire';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                
                // Modèle
                TextFormField(
                  controller: _modeleController,
                  decoration: const InputDecoration(
                    labelText: 'Modèle *',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Le modèle est obligatoire';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                
                // Prix par jour
                TextFormField(
                  controller: _prixController,
                  decoration: const InputDecoration(
                    labelText: 'Prix par jour (€) *',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Le prix est obligatoire';
                    }
                    if (double.tryParse(value) == null || double.parse(value) <= 0) {
                      return 'Le prix doit être un nombre positif';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                
                // Année et Kilométrage
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _anneeController,
                        decoration: const InputDecoration(
                          labelText: 'Année',
                          border: OutlineInputBorder(),
                        ),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: TextFormField(
                        controller: _kilometrageController,
                        decoration: const InputDecoration(
                          labelText: 'Kilométrage',
                          border: OutlineInputBorder(),
                        ),
                        keyboardType: TextInputType.number,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // Carburant et Transmission
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<TypeCarburant>(
                        value: _carburant,
                        decoration: const InputDecoration(
                          labelText: 'Carburant',
                          border: OutlineInputBorder(),
                        ),
                        items: TypeCarburant.values.map((type) {
                          return DropdownMenuItem(
                            value: type,
                            child: Text(_getCarburantDisplayName(type)),
                          );
                        }).toList(),
                        onChanged: (value) {
                          setState(() {
                            _carburant = value;
                          });
                        },
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: DropdownButtonFormField<TypeTransmission>(
                        value: _transmission,
                        decoration: const InputDecoration(
                          labelText: 'Transmission',
                          border: OutlineInputBorder(),
                        ),
                        items: TypeTransmission.values.map((type) {
                          return DropdownMenuItem(
                            value: type,
                            child: Text(_getTransmissionDisplayName(type)),
                          );
                        }).toList(),
                        onChanged: (value) {
                          setState(() {
                            _transmission = value;
                          });
                        },
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // Nombre de places
                TextFormField(
                  controller: _placesController,
                  decoration: const InputDecoration(
                    labelText: 'Nombre de places',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
                const SizedBox(height: 16),
                
                // Description
                TextFormField(
                  controller: _descriptionController,
                  decoration: const InputDecoration(
                    labelText: 'Description',
                    border: OutlineInputBorder(),
                  ),
                  maxLines: 3,
                ),
                const SizedBox(height: 16),
                
                // Disponibilité
                SwitchListTile(
                  title: const Text('Disponible'),
                  subtitle: const Text('La voiture est disponible à la location'),
                  value: _disponible,
                  onChanged: (value) {
                    setState(() {
                      _disponible = value;
                    });
                  },
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Annuler'),
        ),
        ElevatedButton(
          onPressed: _saveVoiture,
          child: Text(isEditing ? 'Modifier' : 'Ajouter'),
        ),
      ],
    );
  }

  void _saveVoiture() {
    if (_formKey.currentState!.validate()) {
      final voitureData = {
        'marque': _marqueController.text,
        'modele': _modeleController.text,
        'prixParJour': double.parse(_prixController.text),
        'annee': _anneeController.text.isNotEmpty ? int.parse(_anneeController.text) : null,
        'kilometrage': _kilometrageController.text.isNotEmpty ? int.parse(_kilometrageController.text) : null,
        'carburant': _carburant?.name,
        'transmission': _transmission?.name,
        'nombrePlaces': _placesController.text.isNotEmpty ? int.parse(_placesController.text) : null,
        'description': _descriptionController.text.isNotEmpty ? _descriptionController.text : null,
        'disponible': _disponible,
      };

      widget.onSave(voitureData);
      Navigator.of(context).pop();
    }
  }

  String _getCarburantDisplayName(TypeCarburant type) {
    switch (type) {
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

  String _getTransmissionDisplayName(TypeTransmission type) {
    switch (type) {
      case TypeTransmission.MANUELLE:
        return 'Manuelle';
      case TypeTransmission.AUTOMATIQUE:
        return 'Automatique';
      case TypeTransmission.SEMI_AUTOMATIQUE:
        return 'Semi-automatique';
    }
  }
}
