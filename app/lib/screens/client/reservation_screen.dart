import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:locme/providers/auth_provider.dart';
import 'package:locme/providers/voiture_provider.dart';
import 'package:locme/providers/reservation_provider.dart';
import 'package:locme/models/voiture.dart';
import 'package:locme/routes.dart';

class ReservationScreen extends StatefulWidget {
  final int? voitureId;

  const ReservationScreen({super.key, this.voitureId});

  @override
  State<ReservationScreen> createState() => _ReservationScreenState();
}

class _ReservationScreenState extends State<ReservationScreen> {
  Voiture? _selectedVoiture;
  DateTime? _dateDebut;
  DateTime? _dateFin;
  final _commentairesController = TextEditingController();
  final _lieuPriseController = TextEditingController();
  final _lieuRetourController = TextEditingController();

  @override
  void initState() {
    super.initState();
    if (widget.voitureId != null) {
      _loadVoiture();
    }
  }

  @override
  void dispose() {
    _commentairesController.dispose();
    _lieuPriseController.dispose();
    _lieuRetourController.dispose();
    super.dispose();
  }

  Future<void> _loadVoiture() async {
    final voiture = await context.read<VoitureProvider>().getVoitureById(widget.voitureId!);
    if (mounted) {
      setState(() {
        _selectedVoiture = voiture;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final authProvider = Provider.of<AuthProvider>(context);
    final reservationProvider = Provider.of<ReservationProvider>(context);
    
    return Scaffold(
      appBar: AppBar(
        title: const Text('Nouvelle réservation'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Voiture selection
            if (_selectedVoiture == null)
              _buildVoitureSelection()
            else
              _buildSelectedVoiture(),
            
            const SizedBox(height: 24),
            
            // Date selection
            _buildDateSelection(),
            
            const SizedBox(height: 24),
            
            // Additional details
            _buildAdditionalDetails(),
            
            const SizedBox(height: 32),
            
            // Total calculation
            if (_selectedVoiture != null && _dateDebut != null && _dateFin != null)
              _buildTotalCalculation(),
            
            const SizedBox(height: 24),
            
            // Create reservation button
            if (_selectedVoiture != null && _dateDebut != null && _dateFin != null)
              ElevatedButton(
                onPressed: reservationProvider.isLoading ? null : _createReservation,
                child: reservationProvider.isLoading
                    ? const CircularProgressIndicator()
                    : const Text('Créer la réservation'),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildVoitureSelection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Sélectionner une voiture',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () => NavigationHelper.goToSearch(context),
              icon: const Icon(Icons.search),
              label: const Text('Rechercher des voitures'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSelectedVoiture() {
    final voiture = _selectedVoiture!;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Voiture sélectionnée',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(
                  Icons.car_rental,
                  size: 48,
                  color: Theme.of(context).colorScheme.primary,
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        voiture.fullName,
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        '${voiture.prixParJour.toStringAsFixed(0)}€/jour',
                        style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        voiture.societe.nom,
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
                TextButton(
                  onPressed: () => NavigationHelper.goToSearch(context),
                  child: const Text('Changer'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDateSelection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Dates de location',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _selectDateDebut,
                    icon: const Icon(Icons.calendar_today),
                    label: Text(_dateDebut != null 
                        ? '${_dateDebut!.day}/${_dateDebut!.month}/${_dateDebut!.year}'
                        : 'Date de début'),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _selectDateFin,
                    icon: const Icon(Icons.calendar_today),
                    label: Text(_dateFin != null 
                        ? '${_dateFin!.day}/${_dateFin!.month}/${_dateFin!.year}'
                        : 'Date de fin'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAdditionalDetails() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Détails supplémentaires',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _lieuPriseController,
              decoration: const InputDecoration(
                labelText: 'Lieu de prise (optionnel)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _lieuRetourController,
              decoration: const InputDecoration(
                labelText: 'Lieu de retour (optionnel)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _commentairesController,
              decoration: const InputDecoration(
                labelText: 'Commentaires (optionnel)',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTotalCalculation() {
    final dureeEnJours = _dateFin!.difference(_dateDebut!).inDays + 1;
    final total = _selectedVoiture!.prixParJour * dureeEnJours;
    
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Résumé de la réservation',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Prix par jour:'),
                Text('${_selectedVoiture!.prixParJour.toStringAsFixed(0)}€'),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Durée:'),
                Text('$dureeEnJours jour${dureeEnJours > 1 ? 's' : ''}'),
              ],
            ),
            const Divider(),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Total:',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  '${total.toStringAsFixed(0)}€',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _selectDateDebut() async {
    final date = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    
    if (date != null) {
      setState(() {
        _dateDebut = date;
        if (_dateFin != null && _dateFin!.isBefore(_dateDebut!)) {
          _dateFin = null;
        }
      });
    }
  }

  Future<void> _selectDateFin() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _dateDebut ?? DateTime.now().add(const Duration(days: 1)),
      firstDate: _dateDebut ?? DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    
    if (date != null) {
      setState(() {
        _dateFin = date;
      });
    }
  }

  Future<void> _createReservation() async {
    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    final reservationProvider = Provider.of<ReservationProvider>(context, listen: false);
    
    if (authProvider.user == null) return;
    
    final success = await reservationProvider.createReservation(
      voiture: _selectedVoiture!,
      user: authProvider.user!,
      dateDebut: _dateDebut!,
      dateFin: _dateFin!,
      commentaires: _commentairesController.text.trim().isEmpty 
          ? null 
          : _commentairesController.text.trim(),
      lieuPrise: _lieuPriseController.text.trim().isEmpty 
          ? null 
          : _lieuPriseController.text.trim(),
      lieuRetour: _lieuRetourController.text.trim().isEmpty 
          ? null 
          : _lieuRetourController.text.trim(),
    );
    
    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Réservation créée avec succès !'),
          backgroundColor: Colors.green,
        ),
      );
      NavigationHelper.goToHome(context);
    }
  }
}


