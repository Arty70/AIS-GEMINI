/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
*/
import { GoogleGenAI } from "@google/genai";
import type { GenerateContentResponse } from "@google/genai";

const API_KEY = process.env.API_KEY;

if (!API_KEY) {
  throw new Error("API_KEY environment variable is not set");
}

const ai = new GoogleGenAI({ apiKey: API_KEY });


// --- Helper Functions ---

/**
 * Creates a primary prompt based on the theme.
 * @param theme The style string (e.g., "1950s", "Classic Comic Hero").
 * @returns The prompt string.
 */
function generatePromptForTheme(theme: string): string {
    if (theme.match(/\d{4}s/)) { // It's a decade
        return `Reimagine the person in this photo in the style of the ${theme}. This includes clothing, hairstyle, photo quality, and the overall aesthetic of that decade. The output must be a photorealistic image showing the person clearly.`;
    }
    
    const lowerTheme = theme.toLowerCase();
    if (lowerTheme.includes('hero') || lowerTheme.includes('guardian') || lowerTheme.includes('sorcerer') || lowerTheme.includes('vigilante')) {
         return `Reimagine the person in this photo as a '${theme}'. Create a unique superhero persona based on this theme. This includes designing a costume that fits the style (e.g., bright and bold for classic comics, textured and detailed for cinematic), putting the person in a dynamic and heroic pose, and creating an evocative background that tells a story about their powers or environment. The output must be a high-quality, photorealistic image showing the person clearly.`;
    }
    
    // Assume the rest are movie genres
    return `Reimagine the person in this photo as the main character in a '${theme}' movie. Capture the quintessential look of that genre, including the specific clothing, hairstyle, and props. Place them in a setting that evokes the genre's atmosphere, using cinematic lighting and mood. The output must be a photorealistic image that looks like a still from the movie.`;
}


/**
 * Creates a fallback prompt to use when the primary one is blocked.
 * @param theme The style string (e.g., "1950s").
 * @returns The fallback prompt string.
 */
function getFallbackPrompt(theme: string): string {
    return `Create a photograph of the person in this image, reimagined in the style of a '${theme}'. The photograph should capture the distinct fashion, setting, and overall atmosphere of that theme. Ensure the final image is a clear photograph that looks authentic to the style.`;
}

/**
 * Processes the Gemini API response, extracting the image or throwing an error if none is found.
 * @param response The response from the generateContent call.
 * @returns A data URL string for the generated image.
 */
function processGeminiResponse(response: GenerateContentResponse): string {
    const imagePartFromResponse = response.candidates?.[0]?.content?.parts?.find(part => part.inlineData);

    if (imagePartFromResponse?.inlineData) {
        const { mimeType, data } = imagePartFromResponse.inlineData;
        return `data:${mimeType};base64,${data}`;
    }

    const textResponse = response.text;
    console.error("API did not return an image. Response:", textResponse);
    throw new Error(`The AI model responded with text instead of an image: "${textResponse || 'No text response received.'}"`);
}

/**
 * A wrapper for the Gemini API call that includes a retry mechanism for internal server errors.
 * @param imagePart The image part of the request payload.
 * @param textPart The text part of the request payload.
 * @returns The GenerateContentResponse from the API.
 */
async function callGeminiWithRetry(imagePart: object, textPart: object): Promise<GenerateContentResponse> {
    const maxRetries = 3;
    const initialDelay = 1000;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return await ai.models.generateContent({
                model: 'gemini-2.5-flash-image',
                contents: { parts: [imagePart, textPart] },
            });
        } catch (error) {
            console.error(`Error calling Gemini API (Attempt ${attempt}/${maxRetries}):`, error);
            const errorMessage = error instanceof Error ? error.message : JSON.stringify(error);
            const isInternalError = errorMessage.includes('"code":500') || errorMessage.includes('INTERNAL');

            if (isInternalError && attempt < maxRetries) {
                const delay = initialDelay * Math.pow(2, attempt - 1);
                console.log(`Internal error detected. Retrying in ${delay}ms...`);
                await new Promise(resolve => setTimeout(resolve, delay));
                continue;
            }
            throw error; // Re-throw if not a retriable error or if max retries are reached.
        }
    }
    // This should be unreachable due to the loop and throw logic above.
    throw new Error("Gemini API call failed after all retries.");
}


/**
 * Generates a styled image from a source image and a theme string.
 * It includes a fallback mechanism for prompts that might be blocked.
 * @param imageDataUrl A data URL string of the source image (e.g., 'data:image/png;base64,...').
 * @param theme The theme/style to guide the image generation (e.g., "1980s", "Film Noir Detective").
 * @returns A promise that resolves to a base64-encoded image data URL of the generated image.
 */
export async function generateStyledImage(imageDataUrl: string, theme: string): Promise<string> {
  const match = imageDataUrl.match(/^data:(image\/\w+);base64,(.*)$/);
  if (!match) {
    throw new Error("Invalid image data URL format. Expected 'data:image/...;base64,...'");
  }
  const [, mimeType, base64Data] = match;

    const imagePart = {
        inlineData: { mimeType, data: base64Data },
    };

    // --- First attempt with the original prompt ---
    try {
        console.log(`Attempting generation with original prompt for ${theme}...`);
        const prompt = generatePromptForTheme(theme);
        const textPart = { text: prompt };
        const response = await callGeminiWithRetry(imagePart, textPart);
        return processGeminiResponse(response);
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : JSON.stringify(error);
        const isNoImageError = errorMessage.includes("The AI model responded with text instead of an image");

        if (isNoImageError) {
            console.warn(`Original prompt for ${theme} was likely blocked. Trying a fallback prompt.`);
            
            // --- Second attempt with the fallback prompt ---
            try {
                const fallbackPrompt = getFallbackPrompt(theme);
                console.log(`Attempting generation with fallback prompt for ${theme}...`);
                const fallbackTextPart = { text: fallbackPrompt };
                const fallbackResponse = await callGeminiWithRetry(imagePart, fallbackTextPart);
                return processGeminiResponse(fallbackResponse);
            } catch (fallbackError) {
                console.error("Fallback prompt also failed.", fallbackError);
                const finalErrorMessage = fallbackError instanceof Error ? fallbackError.message : String(fallbackError);
                throw new Error(`The AI model failed with both original and fallback prompts. Last error: ${finalErrorMessage}`);
            }
        } else {
            // This is for other errors, like a final internal server error after retries.
            console.error("An unrecoverable error occurred during image generation.", error);
            throw new Error(`The AI model failed to generate an image. Details: ${errorMessage}`);
        }
    }
}