import argparse
import os
from cellpose import models, io  # Import the io module for save_rois
from skimage import io as skio

def run_cellpose(inputCellpose, diameterValue):
    # Ensure the directory exists
    if not os.path.exists(inputCellpose):
        print(f"Error: input directory {inputCellpose} does not exist.")
        return

    # Convert the diameter to a float
    diameter = float(diameterValue)
    # Initialize the CellPose model
    model = models.Cellpose(gpu=True, model_type='cyto3')

    # Get a list of image filenames
    filenames = [os.path.join(inputCellpose, f) for f in os.listdir(inputCellpose) if f.endswith(('.png', '.tif', '.jpg'))]
    if not filenames:
        print("No image files found in the directory.")
        return

    # Load images
    images = []
    for filename in filenames:
        image = skio.imread(filename)
        images.append(image)

    # Run CellPose
    masks, flows, styles, diams = model.eval(images, diameter=diameter, channels=[0, 0])

    # Save ROIs
    for i, mask in enumerate(masks):
        full_path = filenames[i]
        dir_path, filename = os.path.split(full_path)
        name, _ = os.path.splitext(filename)
        output_roi_path = os.path.join(dir_path, name + '_rois.zip')

        io.save_rois(mask, output_roi_path)
        print(f"Saved ROI outlines to {output_roi_path}")



if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Run CellPose on image stacks.")
    parser.add_argument('--dir', type=str, required=True, help="Directory where the image stacks are saved.")
    parser.add_argument('--diameter', type=str, required=True, help="Diameter of cells for CellPose.")
    args = parser.parse_args()

    run_cellpose(args.dir, args.diameter)

