package io.mosip.registration.controller.device;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.reg.RegistrationController;
import io.mosip.registration.device.fp.FingerprintFacade;
import io.mosip.registration.device.iris.IrisFacade;
import io.mosip.registration.dto.biometric.FingerprintDetailsDTO;
import io.mosip.registration.dto.biometric.IrisDetailsDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.device.impl.FingerPrintCaptureServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * {@code GuardianBiometricscontroller} is to capture and display the captured
 * biometrics of Guardian
 * 
 * @author Sravya Surampalli
 * @since 1.0
 */
@Controller
public class GuardianBiometricsController extends BaseController implements Initializable{
	
	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(GuardianBiometricsController.class);
	
	@FXML
	private GridPane biometricBox;
	
	@FXML
	private GridPane retryBox;
	
	@FXML
	private ComboBox<String> biometricTypecombo;
	
	@FXML
	private Label biometricType;
	
	@FXML
	private ImageView biometricImage;
	
	@FXML
	private Label qualityScore;
	
	@FXML
	private Label attemptSlap;
	
	@FXML
	private Label thresholdScoreLabel;
	
	@FXML
	private Label thresholdLabel;
	
	@FXML
	private GridPane biometricPane;
	
	@FXML
	private Button scanBtn;
	
	@FXML
	private ProgressBar bioProgress;
	
	@FXML
	private Label qualityText;
	
	@FXML
	private ColumnConstraints thresholdPane1;

	@FXML
	private ColumnConstraints thresholdPane2;
	
	@FXML
	private HBox bioRetryBox;
	
	@FXML
	private Button continueBtn;
	
	@FXML
	private Label duplicateCheckLbl;
	
	/** The scan popup controller. */
	@Autowired
	private ScanPopUpViewController scanPopUpViewController;
	
	/** The registration controller. */
	@Autowired
	private RegistrationController registrationController;
	
	/** The finger print capture service impl. */
	@Autowired
	private FingerPrintCaptureServiceImpl fingerPrintCaptureServiceImpl;
	
	/** The finger print facade. */
	@Autowired
	private FingerprintFacade fingerPrintFacade;
	
	/** The iris facade. */
	@Autowired
	private IrisFacade irisFacade;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize(java.net.URL,
	 * java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Loading of Guardian Biometric screen started");
		
		biometricBox.setVisible(false);	
		retryBox.setVisible(false);
		biometricTypecombo.getItems().removeAll(biometricTypecombo.getItems());
		biometricTypecombo.getItems().addAll("RightHand", "LeftHand", "Thumbs", "RightIris", "LeftIris");
		continueBtn.setDisable(true);
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Loading of Guardian Biometric screen ended");
	}
	
	/**
	 * Displays biometrics
	 *
	 * @param event the event for displaying biometrics
	 */
	@FXML
	private void displayBiometric(ActionEvent event) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Displaying biometrics to capture");
						
		switch (biometricTypecombo.getValue()) {
		case "RightHand":
			updateBiometric("Right Hand", RegistrationConstants.RIGHTPALM_IMG_PATH,
					RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD,
					RegistrationConstants.FINGERPRINT_RETRIES_COUNT);
			break;
		case "LeftHand":
			updateBiometric("Left Hand", RegistrationConstants.LEFTPALM_IMG_PATH,
					RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD,
					RegistrationConstants.FINGERPRINT_RETRIES_COUNT);
			break;
		case "Thumbs":
			updateBiometric("Thumbs", RegistrationConstants.THUMB_IMG_PATH,
					RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD,
					RegistrationConstants.FINGERPRINT_RETRIES_COUNT);
			break;
		case "RightIris":
			updateBiometric("Right Iris", RegistrationConstants.RIGHT_IRIS_IMG_PATH,
					RegistrationConstants.IRIS_THRESHOLD, RegistrationConstants.IRIS_RETRY_COUNT);
			break;
		case "LeftIris":
			updateBiometric("Left Iris", RegistrationConstants.LEFT_IRIS_IMG_PATH, RegistrationConstants.IRIS_THRESHOLD,
					RegistrationConstants.IRIS_RETRY_COUNT);
			break;
		default:

		}
		
		biometricBox.setVisible(true);	
		retryBox.setVisible(true);
		scanBtn.setDisable(false);
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Parent/Guardian Biometrics captured");
	}
	
	/**
	 * Scan the biometrics
	 *
	 * @param event the event for scanning biometrics
	 */
	@FXML
	private void scan(ActionEvent event) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Displaying Scan popup for capturing biometrics");
		
		String headerText = "";
		if(biometricType.getText().contains("Hand") || biometricType.getText().contains("Thumbs")) {
			headerText = RegistrationUIConstants.FINGERPRINT;
		} else if(biometricType.getText().contains("Iris")) {
			headerText = RegistrationUIConstants.IRIS_SCAN;
		}
		scanPopUpViewController.init(this, headerText);
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scan popup closed and captured biometrics");
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.controller.BaseController#scan(javafx.stage.Stage)
	 */
	@Override
	public void scan(Stage popupStage) {
		try {
			
			LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Scan process started for capturing biometrics");
			
			if (biometricType.getText().equalsIgnoreCase("Right Hand")) {
				scanFingers(RegistrationConstants.RIGHTPALM,
						RegistrationConstants.RIGHTHAND_SEGMNTD_DUPLICATE_FILE_PATHS, popupStage, Double.parseDouble(
								getValueFromApplicationContext(RegistrationConstants.RIGHTSLAP_FINGERPRINT_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase("Left Hand")) {
				scanFingers(RegistrationConstants.LEFTPALM,
						RegistrationConstants.LEFTHAND_SEGMNTD_FILE_PATHS_USERONBOARD, popupStage, Double.parseDouble(
								getValueFromApplicationContext(RegistrationConstants.LEFTSLAP_FINGERPRINT_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase("Thumbs")) {
				scanFingers(RegistrationConstants.THUMBS, RegistrationConstants.THUMBS_SEGMNTD_FILE_PATHS_USERONBOARD,
						popupStage, Double.parseDouble(
								getValueFromApplicationContext(RegistrationConstants.THUMBS_FINGERPRINT_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase("Right Iris")) {
				scanIris(RegistrationConstants.RIGHT.concat(RegistrationConstants.EYE), popupStage,
						Double.parseDouble(getValueFromApplicationContext(RegistrationConstants.IRIS_THRESHOLD)));
			} else if (biometricType.getText().equalsIgnoreCase("Left Iris")) {
				scanIris(RegistrationConstants.LEFT.concat(RegistrationConstants.EYE), popupStage,
						Double.parseDouble(getValueFromApplicationContext(RegistrationConstants.IRIS_THRESHOLD)));
			}
			
		} catch (RuntimeException runtimeException) {
			LOGGER.error(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					String.format(
							"Exception while getting the scanned biometrics for user registration: %s caused by %s",
							runtimeException.getMessage(),
							runtimeException.getCause() + ExceptionUtils.getStackTrace(runtimeException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.BIOMETRIC_SCANNING_ERROR);
		} catch (RegBaseCheckedException regBaseCheckedException) {
			LOGGER.error(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID, String.format(
					"Exception while getting the scanned biometrics for user registration: %s caused by %s",
					regBaseCheckedException.getMessage(),
					regBaseCheckedException.getCause() + ExceptionUtils.getStackTrace(regBaseCheckedException)));

			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.BIOMETRIC_SCANNING_ERROR);
		}
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scan process ended for capturing biometrics");
	}

	/**
	 * Navigating to previous section
	 *
	 * @param event the event for navigating to previous section
	 */
	@FXML
	private void previous(ActionEvent event) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Navigates to previous section");
		
		registrationController.showCurrentPage(RegistrationConstants.GUARDIAN_BIOMETRIC,
				getPageDetails(RegistrationConstants.GUARDIAN_BIOMETRIC, RegistrationConstants.PREVIOUS));
	}
	
	/**
	 * Navigating to next section
	 *
	 * @param event the event for navigating to next section
	 */
	@FXML
	private void next(ActionEvent event) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Navigates to next section");
		
		registrationController.showCurrentPage(RegistrationConstants.GUARDIAN_BIOMETRIC,
				getPageDetails(RegistrationConstants.GUARDIAN_BIOMETRIC, RegistrationConstants.NEXT));
	}
	

	/**
	 * Updating biometrics
	 *
	 * @param bioType biometric type
	 * @param bioImage biometric image
	 * @param biometricThreshold threshold of biometric
	 * @param retryCount retry count
	 */
	private void updateBiometric(String bioType, String bioImage, String biometricThreshold, String retryCount) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updating biometrics and clearing previous data");
		
		clearCaptureData();
		biometricType.setText(bioType);
		biometricImage.setImage(new Image(this.getClass().getResourceAsStream(bioImage)));
		thresholdScoreLabel.setText(getQualityScore(Double.parseDouble(
				getValueFromApplicationContext(biometricThreshold))));
		createQualityBox(retryCount, biometricThreshold);
		qualityScore.setText("-");
		attemptSlap.setText("-");
		getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO().getFingerprintDetailsDTO().clear();
		getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO().getIrisDetailsDTO().clear();
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updated biometrics and cleared previous data");
	}
	
	/**
	 * Scan Iris
	 * 
	 * @param irisType iris type
	 * @param popupStage stage
	 * @param thresholdValue threshold value
	 *
	 */
	private void scanIris(String irisType, Stage popupStage, Double thresholdValue)
			throws RegBaseCheckedException {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scanning Iris");

		IrisDetailsDTO detailsDTO = null;

		List<IrisDetailsDTO> irisDetailsDTOs = getRegistrationDTOFromSession().getBiometricDTO()
				.getIntroducerBiometricDTO().getIrisDetailsDTO();

		if (irisDetailsDTOs == null || irisDetailsDTOs.isEmpty()) {
			irisDetailsDTOs = new ArrayList<>(1);
			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					.setIrisDetailsDTO(irisDetailsDTOs);
		}
		if ( irisDetailsDTOs != null) {
			for (IrisDetailsDTO irisDetailsDTO2 : irisDetailsDTOs) {
				if (irisDetailsDTO2.getIrisType().equals(irisType)) {
					detailsDTO = irisDetailsDTO2;
					detailsDTO.setNumOfIrisRetry(irisDetailsDTO2.getNumOfIrisRetry() + 1);
					break;
				}
			}
			if (detailsDTO == null) {
				detailsDTO = new IrisDetailsDTO();
				detailsDTO.setNumOfIrisRetry(detailsDTO.getNumOfIrisRetry() + 1);
				irisDetailsDTOs.add(detailsDTO);
			}
		}
		irisFacade.getIrisImageAsDTO(detailsDTO, irisType);

		scanPopUpViewController.getScanImage().setImage(convertBytesToImage(detailsDTO.getIris()));
		generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.IRIS_SUCCESS_MSG);

		setCapturedValues(detailsDTO.getIris(), detailsDTO.getQualityScore(), detailsDTO.getNumOfIrisRetry(),
				thresholdValue);

		popupStage.close();
		
		if(validateIrisQulaity(detailsDTO, thresholdValue)) {
			scanBtn.setDisable(true);
			continueBtn.setDisable(false);
		} else {
			scanBtn.setDisable(false);
			continueBtn.setDisable(true);
		}
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Iris scanning is completed");

	}
	
	/**
	 * Scan Fingers
	 * 
	 * @param fingerType finger type
	 * @param segmentedFingersPath segmented finger path
	 * @param popupStage stage
	 * @param thresholdValue threshold value
	 */
	private void scanFingers(String fingerType, String[] segmentedFingersPath, Stage popupStage, Double thresholdValue)
			throws RegBaseCheckedException {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Scanning Fingerprints started");
		
		FingerprintDetailsDTO detailsDTO = null;

		List<FingerprintDetailsDTO> fingerprintDetailsDTOs = getRegistrationDTOFromSession().getBiometricDTO()
				.getIntroducerBiometricDTO().getFingerprintDetailsDTO();

		if (fingerprintDetailsDTOs == null || fingerprintDetailsDTOs.isEmpty()) {
			fingerprintDetailsDTOs = new ArrayList<>(1);
			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
					.setFingerprintDetailsDTO(fingerprintDetailsDTOs);
		}

		if (fingerprintDetailsDTOs != null) {

			for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
				if (fingerprintDetailsDTO.getFingerType().equals(fingerType)) {
					detailsDTO = fingerprintDetailsDTO;

					for (String segmentedFingerPath : segmentedFingersPath) {
						String[] path = segmentedFingerPath.split("/");
						for (FingerprintDetailsDTO segmentedfpDetailsDTO : fingerprintDetailsDTO
								.getSegmentedFingerprints()) {
							if (segmentedfpDetailsDTO.getFingerType().equals(path[3])) {
								fingerprintDetailsDTO.getSegmentedFingerprints().remove(segmentedfpDetailsDTO);
								break;
							}
						}
					}
					detailsDTO.setNumRetry(fingerprintDetailsDTO.getNumRetry() + 1);
					break;
				}
			}
			if (detailsDTO == null) {
				detailsDTO = new FingerprintDetailsDTO();
				detailsDTO.setNumRetry(detailsDTO.getNumRetry() + 1);
				fingerprintDetailsDTOs.add(detailsDTO);
			}
		}
		fingerPrintFacade.getFingerPrintImageAsDTO(detailsDTO, fingerType);

		fingerPrintFacade.segmentFingerPrintImage(detailsDTO, segmentedFingersPath);

		scanPopUpViewController.getScanImage().setImage(convertBytesToImage(detailsDTO.getFingerPrint()));

		generateAlert(RegistrationConstants.ALERT_INFORMATION, RegistrationUIConstants.FP_CAPTURE_SUCCESS);

		setCapturedValues(detailsDTO.getFingerPrint(), detailsDTO.getQualityScore(), detailsDTO.getNumRetry(), thresholdValue);
		
		popupStage.close();

		if (validateFingerPrintQulaity(detailsDTO, thresholdValue) && fingerdeduplicationCheck(fingerprintDetailsDTOs)) {
			scanBtn.setDisable(true);
			continueBtn.setDisable(false);
		}
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Fingerprints Scanning is completed");

	}
	
	/**
	 * Updating captured values
	 * 
	 * @param capturedBio biometric
	 * @param qltyScore Qulaity score
	 * @param retry retrycount
	 * @param thresholdValue threshold value
	 */
	private void setCapturedValues(byte[] capturedBio, double qltyScore, int retry, double thresholdValue ) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Upadting captured values of biometrics");
		
		biometricPane.getStyleClass().add(RegistrationConstants.FINGERPRINT_PANES_SELECTED);
		biometricImage.setImage(convertBytesToImage(capturedBio));
		qualityScore.setText(getQualityScore(qltyScore));
		attemptSlap.setText(String.valueOf(retry));
			
		bioProgress.setProgress(Double.valueOf(
					getQualityScore(qltyScore).split(RegistrationConstants.PERCENTAGE)[0]) / 100);
			qualityText.setText(getQualityScore(qltyScore));
			if (Double.valueOf(getQualityScore(qltyScore)
					.split(RegistrationConstants.PERCENTAGE)[0]) >= thresholdValue) {
				clearAttemptsBox(RegistrationConstants.QUALITY_LABEL_GREEN, retry);
				bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_RED);
				bioProgress.getStyleClass().add(RegistrationConstants.PROGRESS_BAR_GREEN);
				qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_RED);
				qualityText.getStyleClass().add(RegistrationConstants.LABEL_GREEN);
			} else {
				clearAttemptsBox(RegistrationConstants.QUALITY_LABEL_RED, retry);
				bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_GREEN);
				bioProgress.getStyleClass().add(RegistrationConstants.PROGRESS_BAR_RED);
				qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_GREEN);
				qualityText.getStyleClass().add(RegistrationConstants.LABEL_RED);
			}
			
			LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Upadted captured values of biometrics");
	}
	
	/**
	 * Updating captured values
	 * 
	 * @param retryCount retry count
	 * @param biometricThreshold threshold value
	 */
	private void createQualityBox(String retryCount, String biometricThreshold) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updating Quality and threshold values of biometrics");
		
		if (!(boolean) SessionContext.map().get(RegistrationConstants.ONBOARD_USER)) {
			for (int retry = 0; retry < Integer.parseInt(
					getValueFromApplicationContext(retryCount)); retry++) {
				Label label = new Label();
				label.getStyleClass().add(RegistrationConstants.QUALITY_LABEL_GREY);
				label.setId(RegistrationConstants.RETRY_ATTEMPT_ID + (retry + 1));
				label.setVisible(true);
				label.setText(String.valueOf(retry + 1));
				label.setAlignment(Pos.CENTER);
				bioRetryBox.getChildren().add(label);
			}

			String threshold = getValueFromApplicationContext(biometricThreshold);

			thresholdLabel.setAlignment(Pos.CENTER);
			thresholdLabel.setText(RegistrationUIConstants.THRESHOLD.concat("  ").concat(threshold)
					.concat(RegistrationConstants.PERCENTAGE));
			thresholdPane1.setPercentWidth(Double.parseDouble(threshold));
			thresholdPane2.setPercentWidth(100.00 - (Double.parseDouble(threshold)));
		}
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Updated Quality and threshold values of biometrics");

	}
	
	/**
	 * Clear attempts box.
	 *
	 * @param styleClass the style class
	 * @param retries    the retries
	 */
	private void clearAttemptsBox(String styleClass, int retries) {
		bioRetryBox.lookup(RegistrationConstants.RETRY_ATTEMPT + retries).getStyleClass().clear();
		bioRetryBox.lookup(RegistrationConstants.RETRY_ATTEMPT + retries).getStyleClass().add(styleClass);
	}

	/**
	 * Clear captured data
	 *
	 */
	private void clearCaptureData() {
		bioProgress.setProgress(0);
		bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_RED);
		bioProgress.getStyleClass().removeAll(RegistrationConstants.PROGRESS_BAR_GREEN);

		qualityText.setText(RegistrationConstants.EMPTY);
		qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_RED);
		qualityText.getStyleClass().removeAll(RegistrationConstants.LABEL_GREEN);

		bioRetryBox.getChildren().clear();
	}
	
	/**
	 * Validates QualityScore.
	 *
	 * @param fingerprintDetailsDTO the fingerprint details DTO
	 * @param handThreshold         the hand threshold
	 * @return boolean
	 */
	private Boolean validateFingerPrintQulaity(FingerprintDetailsDTO fingerprintDetailsDTO, Double handThreshold) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Validating the quality score of the captured fingers");
		
		return fingerprintDetailsDTO.getQualityScore() >= handThreshold
				|| (fingerprintDetailsDTO.getQualityScore() < handThreshold)
						&& fingerprintDetailsDTO.getNumRetry() == Integer.parseInt(
								getValueFromApplicationContext(RegistrationConstants.FINGERPRINT_RETRIES_COUNT))
				|| fingerprintDetailsDTO.isForceCaptured();
	}
	
	/**
	 * Fingerdeduplication check.
	 *
	 * @param segmentedFingerprintDetailsDTOs the segmented fingerprint details
	 *                                        DTO's
	 * @param isValid                         the isvalid flag
	 * @param fingerprintDetailsDTOs          the fingerprint details DT os
	 * @return true, if successful
	 */
	private boolean fingerdeduplicationCheck(List<FingerprintDetailsDTO> fingerprintDetailsDTOs) {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Validating the dedupecheck of the captured fingers");
		
		List<FingerprintDetailsDTO> segmentedFingerprintDetailsDTOs = new ArrayList<>();
		
		boolean isValid = false;
		
		for (FingerprintDetailsDTO fingerprintDetailsDTO : fingerprintDetailsDTOs) {
			for (FingerprintDetailsDTO segmentedFingerprintDetailsDTO : fingerprintDetailsDTO
					.getSegmentedFingerprints()) {
				segmentedFingerprintDetailsDTOs.add(segmentedFingerprintDetailsDTO);
			}
		}
			if (!fingerPrintCaptureServiceImpl.validateFingerprint(segmentedFingerprintDetailsDTOs)) {
				isValid = true;
			} else {
				FingerprintDetailsDTO duplicateFinger = (FingerprintDetailsDTO) SessionContext.map()
						.get(RegistrationConstants.DUPLICATE_FINGER);

				Iterator<FingerprintDetailsDTO> iterator = fingerprintDetailsDTOs.iterator();

				while (iterator.hasNext()) {
					FingerprintDetailsDTO value = iterator.next();
					for (FingerprintDetailsDTO duplicate : value.getSegmentedFingerprints()) {
						if (duplicate.getFingerType().equals(duplicateFinger.getFingerType())) {
							iterator.remove();
							break;
						}
					}
				}
				String finger;
				if (duplicateFinger.getFingerType().contains(RegistrationConstants.LEFT.toLowerCase())) {
					finger = duplicateFinger.getFingerType().replace(RegistrationConstants.LEFT.toLowerCase(),
							RegistrationConstants.LEFT_HAND);
				} else {
					finger = duplicateFinger.getFingerType().replace(RegistrationConstants.RIGHT.toLowerCase(),
							RegistrationConstants.RIGHT_HAND);
				}
				duplicateCheckLbl.setText(finger + " " + RegistrationUIConstants.FINGERPRINT_DUPLICATION_ALERT);
			}
			
			LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validated the dedupecheck of the captured fingers");
			
		return isValid;
	}
	
	/**
	 * Validates QualityScore.
	 *
	 * @param irisDetailsDTO the iris details DTO
	 * @param irisThreshold         the iris threshold
	 * @return boolean
	 */
	private boolean validateIrisQulaity(IrisDetailsDTO irisDetailsDTO, Double irisThreshold) {
		
			LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
					"Validating the quality score of the captured iris");

			return irisDetailsDTO.getQualityScore() >= irisThreshold
					|| (Double.compare(irisDetailsDTO.getQualityScore(), irisThreshold) < 0
							&& irisDetailsDTO.getNumOfIrisRetry() == Integer.parseInt(
									getValueFromApplicationContext(RegistrationConstants.IRIS_RETRY_COUNT))
					|| irisDetailsDTO.isForceCaptured());
	}
	
	/**
	 * Clear Biometric data
	 */
	public void clearCapturedBioData() {
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Clearing the captured biometric data");
		
		if (getRegistrationDTOFromSession() != null) {
			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
			.setFingerprintDetailsDTO(new ArrayList<>());
			
			getRegistrationDTOFromSession().getBiometricDTO().getIntroducerBiometricDTO()
			.setIrisDetailsDTO(new ArrayList<>());
		}
		duplicateCheckLbl.setText(RegistrationConstants.EMPTY);
		clearCaptureData();
		biometricBox.setVisible(false);	
		retryBox.setVisible(false);
		
		LOGGER.info(LOG_REG_GUARDIAN_BIOMETRIC_CONTROLLER, APPLICATION_NAME, APPLICATION_ID,
				"Cleared the captured biometric data");

	}


}
