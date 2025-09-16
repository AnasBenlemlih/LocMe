import 'package:flutter_stripe/flutter_stripe.dart';
import 'package:locme/core/api_client.dart';

class PaymentService {
  static const String publishableKey = 'pk_test_your_stripe_publishable_key_here';
  final ApiClient _apiClient = ApiClient();

  // Initialize Stripe
  static Future<void> initialize() async {
    Stripe.publishableKey = publishableKey;
    await Stripe.instance.applySettings();
  }

  // Create payment intent
  Future<Map<String, dynamic>> createPaymentIntent({
    required double amount,
    required String currency,
    required int reservationId,
  }) async {
    try {
      final response = await _apiClient.createPaymentIntent({
        'amount': (amount * 100).round(), // Convert to cents
        'currency': currency,
        'reservationId': reservationId,
      });

      if (response.statusCode == 200) {
        return response.data;
      } else {
        throw Exception('Failed to create payment intent');
      }
    } catch (e) {
      throw Exception('Payment intent creation failed: ${e.toString()}');
    }
  }

  // Confirm payment
  Future<Map<String, dynamic>> confirmPayment({
    required String paymentIntentId,
  }) async {
    try {
      final response = await _apiClient.confirmPayment(paymentIntentId);

      if (response.statusCode == 200) {
        return response.data;
      } else {
        throw Exception('Failed to confirm payment');
      }
    } catch (e) {
      throw Exception('Payment confirmation failed: ${e.toString()}');
    }
  }

  // Process payment with Stripe
  Future<Map<String, dynamic>> processPayment({
    required double amount,
    required String currency,
    required int reservationId,
  }) async {
    try {
      // Create payment intent
      final paymentIntentData = await createPaymentIntent(
        amount: amount,
        currency: currency,
        reservationId: reservationId,
      );

      final paymentIntent = paymentIntentData['paymentIntent'];

      // Confirm payment with Stripe
      final paymentResult = await Stripe.instance.confirmPayment(
        paymentIntent['client_secret'],
        PaymentMethodParams.card(
          paymentMethodData: PaymentMethodData(
            billingDetails: const BillingDetails(),
          ),
        ),
      );

      if (paymentResult.status == PaymentIntentStatus.Succeeded) {
        // Confirm payment on backend
        await confirmPayment(paymentIntentId: paymentIntent['id']);
        
        return {
          'success': true,
          'paymentIntentId': paymentIntent['id'],
          'message': 'Payment successful',
        };
      } else {
        return {
          'success': false,
          'message': 'Payment failed: ${paymentResult.status}',
        };
      }
    } catch (e) {
      return {
        'success': false,
        'message': 'Payment error: ${e.toString()}',
      };
    }
  }

  // Get payment methods
  Future<List<PaymentMethod>> getPaymentMethods() async {
    try {
      final paymentMethods = await Stripe.instance.retrievePaymentMethods();
      return paymentMethods.data;
    } catch (e) {
      throw Exception('Failed to retrieve payment methods: ${e.toString()}');
    }
  }

  // Save payment method
  Future<PaymentMethod> savePaymentMethod({
    required String cardNumber,
    required int expiryMonth,
    required int expiryYear,
    required String cvc,
  }) async {
    try {
      final paymentMethod = await Stripe.instance.createPaymentMethod(
        params: PaymentMethodParams.card(
          paymentMethodData: PaymentMethodData(
            billingDetails: const BillingDetails(),
          ),
        ),
      );

      return paymentMethod;
    } catch (e) {
      throw Exception('Failed to save payment method: ${e.toString()}');
    }
  }
}
