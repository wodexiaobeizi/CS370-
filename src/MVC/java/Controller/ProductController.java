package Controller;

import Controller.DataPreprocessingController;
import Controller.ErrorHandlingController;
import Controller.InputValidationController;
import Controller.PredictionController;
import Model_classifier.Product;
import Model_classifier.ProductModel;
import View.MainView;
import View.PredictionResultView;
import View.ProductDetailView;

import java.util.List;

public class ProductController {
    private ProductModel productModel;
    private MainView mainView;

    private ProductModel model;
    private PredictionController predictionController;

    private PredictionResultView predictionResultView;

    private DataPreprocessingController dataPreprocessingController;

    private ErrorHandlingController errorHandlingController;

    private InputValidationController inputValidationController;





    public ProductController(ProductModel productModel, PredictionController predictionController, MainView mainView) {
        this.productModel = productModel;
        this.predictionController = predictionController;
        this.mainView = mainView;
        this.predictionResultView = new PredictionResultView();
        this.dataPreprocessingController = new DataPreprocessingController();
        this.errorHandlingController = new ErrorHandlingController();
        this.inputValidationController = new InputValidationController();


    }

    /**
     * Handles the search operation using the provided price range and product name.
     * Displays the filtered products or appropriate error/message to the view.
     *
     * @param priceFrom   The minimum price range.
     * @param priceTo     The maximum price range.
     * @param productName The name of the product to search for.
     * @param view        The view to display the results.
     */
    public void handleSearch(double priceFrom, double priceTo, String productName, MainView view) {
        // Validate the price range
        if (!inputValidationController.validatePrice(priceFrom) || !inputValidationController.validatePrice(priceTo)) {
            view.showError("Invalid price range");
            return;
        }
        try {
            // Get the filtered products
            List<Product> products = productModel.getFilteredProducts(priceFrom, priceTo, productName);

            // Preprocess the products
            List<Product> preprocessedProducts = dataPreprocessingController.preprocessProducts(products);

            if (products.isEmpty()) {
                // If no products found, notify the user
                view.showInfo("No products found in the database matching your criteria.");
            } else {
                // Otherwise, display the products
                view.displayProducts(products);
            }

            // Display the preprocessed products
            view.displayProducts(preprocessedProducts);
        } catch (Exception e) {
            // Handle any exceptions
            errorHandlingController.handleException(e, "Search Operation");
        }
    }

    /**
     * Handles the selection of a product.
     *
     * @param selectedProduct The selected product.
     */
    public void handleProductSelection(Product selectedProduct) {
        // Create an instance of ProductDetailView and pass the required parameters
        ProductDetailView detailView = new ProductDetailView(selectedProduct, predictionController, this);
        detailView.setVisible(true);

        // You can add code here to call the prediction controller
        // For example: String prediction = predictionController.predictProduct(selectedProduct);
    }

    /**
     * Handles the prediction result for a selected product.
     *
     * @param selectedProduct The selected product.
     */
    public void handlePredictionResult(Product selectedProduct) {
        // Predict the product
        String prediction = predictionController.predictProduct(selectedProduct);

        // Display the prediction result
        predictionResultView.displayPredictionResult("Prediction: " + prediction);

        // If the prediction is "No"
        if ("No".equals(prediction)) {
            try {
                // Get recommended products from the same category
                List<Product> recommendedProducts = productModel.getProductsByCategory(selectedProduct.getCategory());

                // Remove the selected product from the recommended products
                recommendedProducts.remove(selectedProduct);

                // Display the recommended products
                mainView.displayRecommendedProducts(recommendedProducts);

                // Notify the user about the update
                mainView.notifyUserAboutUpdate();
            } catch (Exception e) {
                // Handle any exceptions
                errorHandlingController.handleException(e, "Prediction Result");
            }
        }
    }

}
