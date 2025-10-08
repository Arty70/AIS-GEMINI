/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
*/
import React, { useState, ChangeEvent, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { generateStyledImage } from './services/geminiService';
import PolaroidCard from './components/PolaroidCard';
import { createAlbumPage } from './lib/albumUtils';
import Footer from './components/Footer';
import { cn } from './lib/utils';
import html2canvas from 'html2canvas';

const THEME_CATEGORIES = {
    'Decades': ['1950s', '1960s', '1970s', '1980s', '1990s', '2000s'],
    'Superheroes': ['Classic Comic Hero', 'Modern Cinematic Hero', 'Gritty Anti-Hero', 'Cosmic Guardian', 'Mystical Sorcerer', 'Vigilante'],
    'Movie Genres': ['Film Noir Detective', 'Sci-Fi Explorer', 'Fantasy Adventurer', 'Western Gunslinger', '80s Action Hero', 'Rom-Com Lead'],
};
const ALL_THEMES = Object.values(THEME_CATEGORIES).flat();


// Pre-defined positions for a scattered look on desktop
const BASE_POSITIONS = [
    { top: '5%', left: '10%', rotate: -8 },
    { top: '15%', left: '60%', rotate: 5 },
    { top: '45%', left: '5%', rotate: 3 },
    { top: '2%', left: '35%', rotate: 10 },
    { top: '40%', left: '70%', rotate: -12 },
    { top: '50%', left: '38%', rotate: -3 },
];

// Expand positions to accommodate all new themes
const POSITIONS = [
    ...BASE_POSITIONS,
    ...BASE_POSITIONS.map(p => ({ ...p, top: `${parseInt(p.top, 10) + 2}%`, left: `${parseInt(p.left, 10) - 3}%`, rotate: p.rotate + 2 })),
    ...BASE_POSITIONS.map(p => ({ ...p, top: `${parseInt(p.top, 10) - 2}%`, left: `${parseInt(p.left, 10) + 3}%`, rotate: p.rotate - 2 })),
];


const GHOST_POLAROIDS_CONFIG = [
  { initial: { x: "-150%", y: "-100%", rotate: -30 }, transition: { delay: 0.2 } },
  { initial: { x: "150%", y: "-80%", rotate: 25 }, transition: { delay: 0.4 } },
  { initial: { x: "-120%", y: "120%", rotate: 45 }, transition: { delay: 0.6 } },
  { initial: { x: "180%", y: "90%", rotate: -20 }, transition: { delay: 0.8 } },
  { initial: { x: "0%", y: "-200%", rotate: 0 }, transition: { delay: 0.5 } },
  { initial: { x: "100%", y: "150%", rotate: 10 }, transition: { delay: 0.3 } },
];


type ImageStatus = 'pending' | 'done' | 'error';
interface GeneratedImage {
    status: ImageStatus;
    url?: string;
    error?: string;
}

const primaryButtonClasses = "font-permanent-marker text-xl text-center text-black bg-yellow-400 py-3 px-8 rounded-sm transform transition-transform duration-200 hover:scale-105 hover:-rotate-2 hover:bg-yellow-300 shadow-[2px_2px_0px_2px_rgba(0,0,0,0.2)] disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:hover:rotate-0";
const secondaryButtonClasses = "font-permanent-marker text-xl text-center text-white bg-white/10 backdrop-blur-sm border-2 border-white/80 py-3 px-8 rounded-sm transform transition-transform duration-200 hover:scale-105 hover:rotate-2 hover:bg-white hover:text-black disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:hover:rotate-0";

const useMediaQuery = (query: string) => {
    const [matches, setMatches] = useState(false);
    useEffect(() => {
        const media = window.matchMedia(query);
        if (media.matches !== matches) {
            setMatches(media.matches);
        }
        const listener = () => setMatches(media.matches);
        window.addEventListener('resize', listener);
        return () => window.removeEventListener('resize', listener);
    }, [matches, query]);
    return matches;
};

function App() {
    const [uploadedImage, setUploadedImage] = useState<string | null>(null);
    const [generatedImages, setGeneratedImages] = useState<Record<string, GeneratedImage>>({});
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [isDownloading, setIsDownloading] = useState<boolean>(false);
    const [isSavingLayout, setIsSavingLayout] = useState<boolean>(false);
    const [appState, setAppState] = useState<'idle' | 'image-uploaded' | 'generating' | 'results-shown'>('idle');
    const [selectedThemes, setSelectedThemes] = useState<string[]>(ALL_THEMES);
    const [openCategory, setOpenCategory] = useState<string | null>('Decades'); // State for accordion
    const dragAreaRef = useRef<HTMLDivElement>(null);
    const isMobile = useMediaQuery('(max-width: 768px)');


    const handleImageUpload = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            const reader = new FileReader();
            reader.onloadend = () => {
                setUploadedImage(reader.result as string);
                setAppState('image-uploaded');
                setGeneratedImages({}); // Clear previous results
            };
            reader.readAsDataURL(file);
        }
    };

    const handleThemeSelection = (themeToToggle: string) => {
        setSelectedThemes(prev =>
            prev.includes(themeToToggle)
                ? prev.filter(d => d !== themeToToggle)
                : [...prev, themeToToggle]
        );
    };

    const handleSelectAll = () => setSelectedThemes(ALL_THEMES);
    const handleDeselectAll = () => setSelectedThemes([]);


    const handleGenerateClick = async () => {
        if (!uploadedImage || selectedThemes.length === 0) return;

        setIsLoading(true);
        setAppState('generating');
        
        const initialImages: Record<string, GeneratedImage> = {};
        selectedThemes.forEach(theme => {
            initialImages[theme] = { status: 'pending' };
        });
        setGeneratedImages(initialImages);

        const concurrencyLimit = 2; // Process two themes at a time
        const themesQueue = [...selectedThemes];

        const processTheme = async (theme: string) => {
            try {
                const resultUrl = await generateStyledImage(uploadedImage, theme);
                setGeneratedImages(prev => ({
                    ...prev,
                    [theme]: { status: 'done', url: resultUrl },
                }));
            } catch (err) {
                const errorMessage = err instanceof Error ? err.message : "An unknown error occurred.";
                setGeneratedImages(prev => ({
                    ...prev,
                    [theme]: { status: 'error', error: errorMessage },
                }));
                console.error(`Failed to generate image for ${theme}:`, err);
            }
        };

        const workers = Array(concurrencyLimit).fill(null).map(async () => {
            while (themesQueue.length > 0) {
                const theme = themesQueue.shift();
                if (theme) {
                    await processTheme(theme);
                }
            }
        });

        await Promise.all(workers);

        setIsLoading(false);
        setAppState('results-shown');
    };

    const handleRegenerateTheme = async (theme: string) => {
        if (!uploadedImage) return;

        // Prevent re-triggering if a generation is already in progress
        if (generatedImages[theme]?.status === 'pending') {
            return;
        }
        
        console.log(`Regenerating image for ${theme}...`);

        // Set the specific theme to 'pending' to show the loading spinner
        setGeneratedImages(prev => ({
            ...prev,
            [theme]: { status: 'pending' },
        }));

        // Call the generation service for the specific theme
        try {
            const resultUrl = await generateStyledImage(uploadedImage, theme);
            setGeneratedImages(prev => ({
                ...prev,
                [theme]: { status: 'done', url: resultUrl },
            }));
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : "An unknown error occurred.";
            setGeneratedImages(prev => ({
                ...prev,
                [theme]: { status: 'error', error: errorMessage },
            }));
            console.error(`Failed to regenerate image for ${theme}:`, err);
        }
    };
    
    const handleReset = () => {
        setUploadedImage(null);
        setGeneratedImages({});
        setAppState('idle');
        setSelectedThemes(ALL_THEMES);
    };

    const handleDownloadIndividualImage = (theme: string) => {
        const image = generatedImages[theme];
        if (image?.status === 'done' && image.url) {
            const link = document.createElement('a');
            link.href = image.url;
            link.download = `past-forward-${theme.toLowerCase().replace(/\s+/g, '-')}.jpg`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    };

    const handleDownloadAlbum = async () => {
        setIsDownloading(true);
        try {
            // FIX: Explicitly cast `image` to `GeneratedImage` to resolve TypeScript error where
            // it's inferred as `unknown` from Object.entries.
            const imageData = Object.entries(generatedImages)
                .filter(([, image]) => (image as GeneratedImage).status === 'done' && (image as GeneratedImage).url)
                .reduce((acc, [theme, image]) => {
                    acc[theme] = (image as GeneratedImage).url!;
                    return acc;
                }, {} as Record<string, string>);

            if (Object.keys(imageData).length < Object.keys(generatedImages).length) {
                alert("Please wait for all selected images to finish generating before downloading the album.");
                setIsDownloading(false);
                return;
            }

            const albumDataUrl = await createAlbumPage(imageData);

            const link = document.createElement('a');
            link.href = albumDataUrl;
            link.download = 'past-forward-album.jpg';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

        } catch (error) {
            console.error("Failed to create or download album:", error);
            alert("Sorry, there was an error creating your album. Please try again.");
        } finally {
            setIsDownloading(false);
        }
    };
    
    const handleSaveLayout = async () => {
        if (!dragAreaRef.current) return;
        setIsSavingLayout(true);
        try {
            // Temporarily hide on-card buttons for a cleaner capture
            const buttons = dragAreaRef.current.querySelectorAll('button');
            buttons.forEach(btn => btn.style.opacity = '0');

            const canvas = await html2canvas(dragAreaRef.current, {
                backgroundColor: '#000000', // Match the app's background
                useCORS: true,
                logging: false,
                scale: window.devicePixelRatio * 2, // Increase resolution for sharper images
            });
            
            // Restore button visibility
            buttons.forEach(btn => btn.style.opacity = '');

            const dataUrl = canvas.toDataURL('image/jpeg', 0.9);

            const link = document.createElement('a');
            link.href = dataUrl;
            link.download = 'past-forward-layout.jpg';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (error) {
            console.error("Failed to save layout:", error);
            alert("Sorry, there was an error saving your layout. Please try again.");
        } finally {
            setIsSavingLayout(false);
        }
    };

    return (
        <main className="bg-black text-neutral-200 min-h-screen w-full flex flex-col items-center justify-center p-4 pb-24 overflow-hidden relative">
            <div className="absolute top-0 left-0 w-full h-full bg-grid-white/[0.05]"></div>
            
            <div className="z-10 flex flex-col items-center justify-center w-full h-full flex-1 min-h-0">
                <div className="text-center mb-10">
                    <h1 className="text-6xl md:text-8xl font-caveat font-bold text-neutral-100">Past Forward</h1>
                    <p className="font-permanent-marker text-neutral-300 mt-2 text-xl tracking-wide">Reimagine yourself in different styles.</p>
                </div>

                {appState === 'idle' && (
                     <div className="relative flex flex-col items-center justify-center w-full">
                        {/* Ghost polaroids for intro animation */}
                        {GHOST_POLAROIDS_CONFIG.map((config, index) => (
                             <motion.div
                                key={index}
                                className="absolute w-80 h-[26rem] rounded-md p-4 bg-neutral-100/10 blur-sm"
                                initial={config.initial}
                                animate={{
                                    x: "0%", y: "0%", rotate: (Math.random() - 0.5) * 20,
                                    scale: 0,
                                    opacity: 0,
                                }}
                                transition={{
                                    ...config.transition,
                                    ease: "circOut",
                                    duration: 2,
                                }}
                            />
                        ))}
                        <motion.div
                             initial={{ opacity: 0, scale: 0.8 }}
                             animate={{ opacity: 1, scale: 1 }}
                             transition={{ delay: 2, duration: 0.8, type: 'spring' }}
                             className="flex flex-col items-center"
                        >
                            <label htmlFor="file-upload" className="cursor-pointer group transform hover:scale-105 transition-transform duration-300">
                                 <PolaroidCard 
                                     caption="Click to begin"
                                     status="done"
                                 />
                            </label>
                            <input id="file-upload" type="file" className="hidden" accept="image/png, image/jpeg, image/webp" onChange={handleImageUpload} />
                            <p className="mt-8 font-permanent-marker text-neutral-500 text-center max-w-xs text-lg">
                                Click the polaroid to upload your photo and start your journey through time.
                            </p>
                        </motion.div>
                    </div>
                )}

                {appState === 'image-uploaded' && uploadedImage && (
                    <div className="flex flex-col items-center gap-6">
                         <PolaroidCard 
                            imageUrl={uploadedImage} 
                            caption="Your Photo" 
                            status="done"
                         />

                        <motion.div 
                            className="flex flex-col items-center gap-2 mt-4 w-full max-w-3xl px-4"
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.5, delay: 0.2 }}
                        >
                            <p className="font-permanent-marker text-neutral-300 text-lg mb-2">Which styles should we try?</p>
                             {Object.entries(THEME_CATEGORIES).map(([category, themes]) => (
                                <div key={category} className="w-full bg-white/5 rounded-md overflow-hidden">
                                     <button
                                        onClick={() => setOpenCategory(openCategory === category ? null : category)}
                                        className="w-full flex justify-between items-center p-4 text-left hover:bg-white/10 transition-colors duration-200"
                                        aria-expanded={openCategory === category}
                                    >
                                        <h3 className="font-permanent-marker text-yellow-400 text-xl">{category}</h3>
                                        <motion.div
                                            animate={{ rotate: openCategory === category ? 180 : 0 }}
                                            transition={{ duration: 0.2 }}
                                        >
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                                            </svg>
                                        </motion.div>
                                    </button>
                                     <AnimatePresence initial={false}>
                                        {openCategory === category && (
                                            <motion.div
                                                key="content"
                                                initial="collapsed"
                                                animate="open"
                                                exit="collapsed"
                                                variants={{
                                                    open: { opacity: 1, height: "auto" },
                                                    collapsed: { opacity: 0, height: 0 }
                                                }}
                                                transition={{ duration: 0.3, ease: [0.04, 0.62, 0.23, 0.98] }}
                                                className="overflow-hidden"
                                            >
                                                <div className="p-4 pt-0">
                                                    <div className="flex flex-wrap justify-start gap-3">
                                                        {themes.map(theme => (
                                                            <button
                                                                key={theme}
                                                                onClick={() => handleThemeSelection(theme)}
                                                                className={cn(
                                                                    "font-permanent-marker text-sm sm:text-base py-2 px-3 rounded-sm transition-all duration-200 transform hover:scale-105",
                                                                    selectedThemes.includes(theme)
                                                                        ? 'bg-yellow-400 text-black shadow-[1px_1px_0px_1px_rgba(0,0,0,0.2)] hover:bg-yellow-300'
                                                                        : 'bg-white/10 border border-white/50 text-white hover:bg-white/20'
                                                                )}
                                                                aria-pressed={selectedThemes.includes(theme)}
                                                            >
                                                                {theme}
                                                            </button>
                                                        ))}
                                                    </div>
                                                </div>
                                            </motion.div>
                                        )}
                                    </AnimatePresence>
                                </div>
                            ))}
                            <div className="flex gap-6 text-sm mt-4">
                                <button onClick={handleSelectAll} className="text-neutral-400 hover:text-white transition-colors duration-200 underline">Select All</button>
                                <button onClick={handleDeselectAll} className="text-neutral-400 hover:text-white transition-colors duration-200 underline">Deselect All</button>
                            </div>
                        </motion.div>

                         <div className="flex items-center gap-4 mt-4">
                            <button onClick={handleReset} className={secondaryButtonClasses}>
                                Different Photo
                            </button>
                            <button 
                                onClick={handleGenerateClick} 
                                disabled={selectedThemes.length === 0}
                                className={primaryButtonClasses}
                            >
                                Generate
                            </button>
                         </div>
                    </div>
                )}

                {(appState === 'generating' || appState === 'results-shown') && (
                     <>
                        {isMobile ? (
                            <div className="w-full max-w-sm flex-1 overflow-y-auto mt-4 space-y-8 p-4">
                                {ALL_THEMES.map((theme) => {
                                    if (!generatedImages[theme]) return null;
                                    return (
                                    <div key={theme} className="flex justify-center">
                                         <PolaroidCard
                                            caption={theme}
                                            status={generatedImages[theme]?.status || 'pending'}
                                            imageUrl={generatedImages[theme]?.url}
                                            error={generatedImages[theme]?.error}
                                            onShake={handleRegenerateTheme}
                                            onDownload={handleDownloadIndividualImage}
                                            isMobile={isMobile}
                                        />
                                    </div>
                                )})}
                            </div>
                        ) : (
                            <div ref={dragAreaRef} className="relative w-full max-w-5xl h-[600px] mt-4">
                                {ALL_THEMES.map((theme, index) => {
                                    if (!generatedImages[theme]) return null;
                                    const { top, left, rotate } = POSITIONS[index % POSITIONS.length];
                                    return (
                                        <motion.div
                                            key={theme}
                                            className="absolute cursor-grab active:cursor-grabbing"
                                            style={{ top, left }}
                                            initial={{ opacity: 0, scale: 0.5, y: 100, rotate: 0 }}
                                            animate={{ 
                                                opacity: 1, 
                                                scale: 1, 
                                                y: 0,
                                                rotate: `${rotate}deg`,
                                            }}
                                            transition={{ type: 'spring', stiffness: 100, damping: 20, delay: Object.keys(generatedImages).indexOf(theme) * 0.15 }}
                                        >
                                            <PolaroidCard 
                                                dragConstraintsRef={dragAreaRef}
                                                caption={theme}
                                                status={generatedImages[theme]?.status || 'pending'}
                                                imageUrl={generatedImages[theme]?.url}
                                                error={generatedImages[theme]?.error}
                                                onShake={handleRegenerateTheme}
                                                onDownload={handleDownloadIndividualImage}
                                                isMobile={isMobile}
                                            />
                                        </motion.div>
                                    );
                                })}
                            </div>
                        )}
                         <div className="h-20 mt-4 flex items-center justify-center">
                            {appState === 'results-shown' && (
                                <div className="flex flex-col sm:flex-row items-center gap-4">
                                    <button 
                                        onClick={handleDownloadAlbum} 
                                        disabled={isDownloading} 
                                        className={primaryButtonClasses}
                                    >
                                        {isDownloading ? 'Creating Album...' : 'Download Album'}
                                    </button>
                                     {!isMobile && (
                                        <button
                                            onClick={handleSaveLayout}
                                            disabled={isSavingLayout}
                                            className={secondaryButtonClasses}
                                        >
                                            {isSavingLayout ? 'Saving Layout...' : 'Save Layout'}
                                        </button>
                                    )}
                                    <button onClick={handleReset} className={secondaryButtonClasses}>
                                        Start Over
                                    </button>
                                </div>
                            )}
                        </div>
                    </>
                )}
            </div>
            <Footer />
        </main>
    );
}

export default App;