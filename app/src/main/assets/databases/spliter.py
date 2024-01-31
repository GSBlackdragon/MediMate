import csv

def split_csv_file(input_file_path, num_files=3):
    files = [open(f'split_{i}.csv', 'w', newline='', encoding='utf-8') for i in range(num_files)]
    writers = [csv.writer(file) for file in files]

    with open(input_file_path, 'r', newline='', encoding='utf-8') as csvfile:
        reader = csv.reader(csvfile)
        headers = next(reader)  # Assumant que le fichier a une ligne d'en-tête

        # Écrire les en-têtes dans les nouveaux fichiers
        for writer in writers:
            writer.writerow(headers)

        # Répartir les lignes entre les nouveaux fichiers
        for i, row in enumerate(reader):
            writers[i % num_files].writerow(row)

    # Fermer tous les fichiers ouverts
    for file in files:
        file.close()

# Exemple d'utilisation
input_file_path = 'doctor_flat.csv'
output_path = 'path/to/output/directory'
split_csv_file(input_file_path)
